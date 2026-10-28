"""Probe what a signed-in outside account can do through the public test contributor App.

Run it, open the printed page while signed in as the outside test account, and enter the code.
The user token stays in memory: it is never printed or written to disk. Every step prints the
HTTP status and GitHub's message, so a refusal is evidence rather than a crash.

    python scripts/rumble/contributor_probe.py

Only the test environment is touched (robocode-dev/rumble-data-test and rumble-bots-test).
"""

import base64
import json
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid

from rumble_environments import TEST

CLIENT_ID = "Iv23likgKJHOPyEQABJs"
OWNER = "robocode-dev"
DATA = f"{OWNER}/{TEST.data_repository}"
BOTS = f"{OWNER}/{TEST.bots_repository}"


def call(method, url, token=None, body=None, form=False):
    headers = {"Accept": "application/vnd.github+json", "User-Agent": "rumble-contributor-probe"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    data = None
    if body is not None:
        if form:
            data = urllib.parse.urlencode(body).encode()
        else:
            data = json.dumps(body).encode()
            headers["Content-Type"] = "application/json"
    request = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(request) as response:
            return response.status, json.loads(response.read() or b"{}")
    except urllib.error.HTTPError as error:
        try:
            return error.code, json.loads(error.read() or b"{}")
        except ValueError:
            return error.code, {}


def sign_in():
    status, grant = call("POST", "https://github.com/login/device/code", body={"client_id": CLIENT_ID}, form=True)
    if status != 200 or "device_code" not in grant:
        sys.exit(f"Device flow was refused: {status} {grant}")
    print(f"Open {grant['verification_uri']} as the outside test account and enter {grant['user_code']}")
    interval = grant["interval"]
    deadline = time.time() + grant["expires_in"]
    while time.time() < deadline:
        time.sleep(interval)
        status, answer = call("POST", "https://github.com/login/oauth/access_token", body={
            "client_id": CLIENT_ID, "device_code": grant["device_code"],
            "grant_type": "urn:ietf:params:oauth:grant-type:device_code"}, form=True)
        if "access_token" in answer:
            return answer["access_token"], answer
        if answer.get("error") == "slow_down":
            interval += 5
        elif answer.get("error") != "authorization_pending":
            sys.exit(f"Sign-in failed: {answer}")
    sys.exit("The sign-in code expired.")


def wait_for_receipt(token, number, minutes=4):
    """Return the first reply on an issue and whether it was closed, or say that none arrived."""
    url = f"https://api.github.com/repos/{DATA}/issues/{number}"
    deadline = time.time() + minutes * 60
    while time.time() < deadline:
        time.sleep(10)
        _, comments = call("GET", url + "/comments", token)
        if isinstance(comments, list) and comments:
            _, issue = call("GET", url, token)
            return f"{comments[0]['body'].strip()!r} (issue {issue.get('state')})"
    return "none within the wait"


def report(step, outcome):
    status, answer = outcome
    note = answer.get("message") or ""
    print(f"[{status}] {step}" + (f": {note}" if note else ""))
    return outcome


def watch_for_revocation(token, minutes=6):
    """Poll until GitHub stops accepting the token, which happens when the participant revokes the App."""
    print("Revoke the App now at https://github.com/settings/connections/applications as this account.")
    deadline = time.time() + minutes * 60
    while time.time() < deadline:
        status, answer = call("GET", "https://api.github.com/user", token)
        if status != 200:
            print(f"[{status}] the token stopped working after revocation: {answer.get('message')}")
            status, _ = call("POST", f"https://api.github.com/repos/{DATA}/issues", token,
                             {"title": "[register] after revocation", "body": "must be refused"})
            print(f"[{status}] opening an issue with the revoked token")
            return
        time.sleep(10)
    print("The token still worked at the end of the wait.")


def main():
    token, grant = sign_in()
    if "--revocation" in sys.argv:
        return watch_for_revocation(token)
    print("token lifetime (seconds):", grant.get("expires_in", "none, does not expire"),
          "| refresh token:", "yes" if grant.get("refresh_token") else "no")
    status, user = report("who is signed in", call("GET", "https://api.github.com/user", token))
    if status == 200:
        print("  login:", user["login"])
    status, installs = report("installations visible to this sign-in",
                              call("GET", "https://api.github.com/user/installations", token))
    for installation in installs.get("installations", []):
        print("  installed on:", installation["account"]["login"], installation["repository_selection"])

    status, registration = report("open a registration issue", call(
        "POST", f"https://api.github.com/repos/{DATA}/issues", token,
        {"title": "[register] sign-in experiment", "body": "Register this account."}))
    client_id = None
    if status == 201:
        receipt = wait_for_receipt(token, registration["number"])
        print("  receipt:", receipt)
        found = re.search(r"client ID `([^`]+)`", receipt)
        client_id = found.group(1) if found else None
    if not client_id:
        _, existing = call("GET", f"https://api.github.com/repos/{DATA}/contents/clients/{user['login']}.json", None)
        if "content" in existing:
            client_id = json.loads(base64.b64decode(existing["content"]))["clientIds"][0]
            print("  registration was refused, using the account's existing client ID:", client_id)
    if client_id:
        submit_batches(token, client_id)

    submit_bot_change(token, user["login"])
    refresh_without_secret(grant)


def valid_record(client_id, battle_id, bots):
    """Build one ranked 1v1 result record that matches the test engine pin and catalog."""
    def entry(bot, rank, score, firsts, seconds):
        return {"name": bot["name"], "version": bot["version"], "isTeam": False, "rank": rank, "totalScore": score,
                "survival": 0, "lastSurvivorBonus": 0, "bulletDamage": 0, "bulletKillBonus": 0, "ramDamage": 0,
                "ramKillBonus": 0, "firstPlaces": firsts, "secondPlaces": seconds, "thirdPlaces": 0}
    return {"battleId": battle_id, "completedAt": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
            "client": {"id": client_id, "version": "0.1.0"}, "engine": {"behaviorVersion": 1},
            "gameType": "1v1", "rounds": 35, "arenaWidth": 800, "arenaHeight": 600,
            "participants": [entry(bots[0], 1, 80, 35, 0), entry(bots[1], 2, 20, 0, 35)]}


def submit_batches(token, client_id):
    """Submit a valid batch, one under a client ID the account does not own, and a clashing battle ID."""
    status, catalog = call("GET", f"https://api.github.com/repos/{DATA}/contents/catalog.json", None)
    bots = [b for b in json.loads(base64.b64decode(catalog["content"]))["bots"]
            if b.get("status") == "active" and not b.get("teamMembers")][:2]
    battle_id = str(uuid.uuid4())
    valid = valid_record(client_id, battle_id, bots)
    stolen = valid_record("example-contributor-local", str(uuid.uuid4()), bots)
    clash = valid_record(client_id, battle_id, bots)
    clash["participants"][0]["totalScore"] = 81
    for step, record, cid in (("a valid ranked batch", valid, client_id),
                              ("a batch under a client ID owned by another account", stolen, "example-contributor-local"),
                              ("the same battle ID with different content", clash, client_id)):
        envelope = {"schemaVersion": 1, "clientId": cid, "clientVersion": "0.1.0", "results": [record]}
        status, issue = report(f"submit {step}", call(
            "POST", f"https://api.github.com/repos/{DATA}/issues", token,
            {"title": "[result] sign-in experiment", "body": "```json\n" + json.dumps(envelope) + "\n```"}))
        if status == 201:
            print("  receipt:", wait_for_receipt(token, issue["number"], minutes=3))


def submit_bot_change(token, login):
    """Fork the bot repository, push a branch with one file and open a pull request."""
    api = "https://api.github.com"
    fork = f"{login}/{TEST.bots_repository}"
    status, _ = call("GET", f"{api}/repos/{fork}", token)
    if status == 200:
        print(f"[200] use the fork the account made in the web page: {fork}")
    else:
        status, _ = report("fork the bot repository", call("POST", f"{api}/repos/{BOTS}/forks", token, {}))
        if status not in (200, 202):
            return
    for _ in range(30):
        time.sleep(4)
        status, _ = call("GET", f"{api}/repos/{fork}", token)
        if status == 200:
            break
    status, main = report("read the fork's main branch", call("GET", f"{api}/repos/{fork}/git/ref/heads/main", token))
    if status != 200:
        return
    branch = f"probe-sign-in-{int(time.time())}"
    status, _ = report("create a branch in the fork", call("POST", f"{api}/repos/{fork}/git/refs", token, {
        "ref": f"refs/heads/{branch}", "sha": main["object"]["sha"]}))
    if status != 201:
        return
    content = base64.b64encode(b"sign-in experiment, ignore\n").decode()
    status, _ = report("push a file to the fork", call("PUT", f"{api}/repos/{fork}/contents/probe-sign-in.txt", token, {
        "message": "Sign-in experiment", "content": content, "branch": branch}))
    if status not in (200, 201):
        return
    status, pull = report("open a pull request against the bot repository", call("POST", f"{api}/repos/{BOTS}/pulls", token, {
        "title": "Sign-in experiment, ignore", "head": f"{login}:{branch}", "base": "main",
        "body": "Opened by the contributor probe to test fork and pull request rights."}))
    if status == 201:
        print("  pull request:", pull["html_url"])


def refresh_without_secret(grant):
    """Renewing a user token normally needs the App's client secret; check whether it works without one."""
    if not grant.get("refresh_token"):
        return
    answer = call("POST", "https://github.com/login/oauth/access_token", None, {
        "client_id": CLIENT_ID, "grant_type": "refresh_token", "refresh_token": grant["refresh_token"]}, form=True)
    outcome = "renewed without a secret" if "access_token" in answer[1] else f"refused: {answer[1].get('error')}"
    print("[%s] renew the sign-in with the refresh token and no client secret: %s" % (answer[0], outcome))


if __name__ == "__main__":
    main()
