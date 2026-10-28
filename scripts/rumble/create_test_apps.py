"""Create the two test-environment GitHub Apps and store their credentials as repository secrets.

GitHub cannot copy an App, but it can create one from a prepared settings file. This script
serves a local page with one button per App. After an organization owner presses a button and
confirms in GitHub, GitHub redirects back here, the script exchanges the one-time code for the
App's credentials and stores them directly as secrets of the test bot repository, so no key
is ever shown, saved to disk or copied by hand. Installing each App on the test repositories
is still done in the browser, because GitHub offers no way to automate it.

Usage:
    python scripts/rumble/create_test_apps.py [moderator] [publisher] [contributor]

Without names it creates the moderator and publisher Apps. The contributor App is the one an
outside participant signs in with on their own computer. It is public, so any account can
install it, and it has no secrets stored anywhere. Its device sign-in must still be switched
on by hand in the App's settings, because the settings file cannot do that.

Requires `gh` signed in as an owner of the organization, and the test repositories to exist.
"""
import html
import http.server
import json
import secrets
import subprocess
import sys
import threading
import urllib.parse
import urllib.request
import webbrowser

from rumble_environments import OWNER, TEST

PORT = 8765
DEFAULT_APPS = ("moderator", "publisher")
SECRET_REPOSITORY = f"{OWNER}/{TEST.bots_repository}"

# Same permissions as the public Apps, which were read from the live Apps on 2026-10-27.
APPS = {
    "moderator": {
        "name": "Tank Royale Rumble Moderator Test",
        "description": "Test environment only. Reviews Rumble bot submissions, runs a test battle, and merges those that pass.",
        "permissions": {"contents": "write", "issues": "write", "members": "read",
                        "metadata": "read", "pull_requests": "write", "statuses": "write"},
        "secret_prefix": "RUMBLE_MODERATOR",
    },
    "publisher": {
        "name": "Tank Royale Rumble Publisher Test",
        "description": "Test environment only. Opens generated Rumble catalog pull requests after a bot submission is merged.",
        "permissions": {"contents": "read", "metadata": "read", "pull_requests": "write"},
        "secret_prefix": "RUMBLE_PUBLISHER",
    },
    "contributor": {
        "name": "Tank Royale Rumble Contrib Test",
        "description": "Test environment only. Lets a participant sign in from their own computer to submit Rumble bots and battle results.",
        "permissions": {"issues": "write", "pull_requests": "write", "contents": "write", "metadata": "read"},
        "secret_prefix": None,
        "public": True,
    },
}


def manifest(key, port=PORT):
    app = APPS[key]
    return {
        "name": app["name"],
        "url": f"https://github.com/{SECRET_REPOSITORY}",
        "description": app["description"],
        "public": app.get("public", False),
        "redirect_url": f"http://localhost:{port}/created/{key}",
        "hook_attributes": {"url": "https://example.invalid/unused", "active": False},
        "default_permissions": app["permissions"],
        "default_events": [],
    }


def page(state, keys=None, port=PORT):
    forms = []
    for key in keys or DEFAULT_APPS:
        action = f"https://github.com/organizations/{OWNER}/settings/apps/new?state={state}"
        data = html.escape(json.dumps(manifest(key, port)), quote=True)
        forms.append(
            f'<form method="post" action="{action}"><input type="hidden" name="manifest" value="{data}">'
            f'<button>Create {html.escape(APPS[key]["name"])}</button></form>')
    return "<!doctype html><meta charset=utf-8><title>Create test Apps</title>" + "".join(forms)


def convert(code):
    request = urllib.request.Request(
        f"https://api.github.com/app-manifests/{urllib.parse.quote(code)}/conversions",
        method="POST", headers={"Accept": "application/vnd.github+json", "User-Agent": "tank-royale-rumble-setup"})
    with urllib.request.urlopen(request) as response:
        return json.load(response)


def store(key, created):
    prefix = APPS[key]["secret_prefix"]
    if prefix is None:
        return
    for name, value in ((f"{prefix}_APP_ID", str(created["id"])), (f"{prefix}_PRIVATE_KEY", created["pem"])):
        subprocess.run(["gh", "secret", "set", name, "--repo", SECRET_REPOSITORY],
                       input=value, text=True, check=True)


def main(keys):
    state = secrets.token_urlsafe(24)
    done = set()
    finished = threading.Event()

    class Handler(http.server.BaseHTTPRequestHandler):
        def log_message(self, *args):
            pass

        def reply(self, status, text):
            body = text.encode("utf-8")
            self.send_response(status)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)

        def do_GET(self):
            url = urllib.parse.urlparse(self.path)
            query = urllib.parse.parse_qs(url.query)
            if url.path == "/":
                return self.reply(200, page(state, keys))
            parts = url.path.strip("/").split("/")
            if len(parts) == 2 and parts[0] == "created" and parts[1] in keys:
                if query.get("state") != [state] or "code" not in query:
                    return self.reply(400, "Unexpected request, nothing was stored.")
                created = convert(query["code"][0])
                store(parts[1], created)
                done.add(parts[1])
                if APPS[parts[1]]["secret_prefix"] is None:
                    print(f"Created {created['slug']}. Nothing was stored; it has no secrets.")
                else:
                    print(f"Created {created['slug']} and stored its credentials as secrets of {SECRET_REPOSITORY}.")
                print(f"Install it on the test repositories: https://github.com/apps/{created['slug']}/installations/new")
                if done == set(keys):
                    finished.set()
                return self.reply(200, f"{html.escape(created['slug'])} created. You can close this tab.")
            return self.reply(404, "Not found.")

    server = http.server.ThreadingHTTPServer(("127.0.0.1", PORT), Handler)
    threading.Thread(target=server.serve_forever, daemon=True).start()
    print(f"Open http://localhost:{PORT}/ (opening it now) and press both buttons.")
    webbrowser.open(f"http://localhost:{PORT}/")
    finished.wait()
    server.shutdown()
    return 0


if __name__ == "__main__":
    wanted = tuple(sys.argv[1:]) or DEFAULT_APPS
    unknown = [k for k in wanted if k not in APPS]
    if unknown:
        sys.exit(f"Unknown App name(s): {unknown}. Choose from {list(APPS)}.")
    sys.exit(main(wanted))
