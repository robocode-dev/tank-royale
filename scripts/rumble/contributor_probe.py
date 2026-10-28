"""Probe what a signed-in outside account can do through the public test contributor App.

Run it, open the printed page while signed in as the outside test account, and enter the code.
The user token stays in memory: it is never printed or written to disk. Every step prints the
HTTP status and GitHub's message, so a refusal is evidence rather than a crash.

    python scripts/rumble/contributor_probe.py

Only the test environment is touched (robocode-dev/rumble-data-test and rumble-bots-test).
"""

import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

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


def main():
    token, grant = sign_in()
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
    if status == 201:
        print("  receipt:", wait_for_receipt(token, registration["number"]))

    status, batch = report("open a result issue without any label", call(
        "POST", f"https://api.github.com/repos/{DATA}/issues", token,
        {"title": "[result] sign-in experiment",
         "body": "```json\n{\"probe\": true}\n```"}))
    if status == 201:
        print("  receipt:", wait_for_receipt(token, batch["number"]))

    report("fork the bot repository", call("POST", f"https://api.github.com/repos/{BOTS}/forks", token, {}))


if __name__ == "__main__":
    main()
