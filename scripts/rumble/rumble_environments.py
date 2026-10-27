"""The two Rumble environments and the reset that is allowed in only one of them.

The public Rumble is the Beta that real participants use. The test Rumble has its own
repositories, so experiments and resets can never reach public data. Every decision here
fails closed: a repository counts as a test repository only when its full name matches the
test environment exactly.

Usage:
    python scripts/rumble/rumble_environments.py reset <checkout-of-rumble-data-test> [--apply]
"""
import argparse
import json
from pathlib import Path
import re
import subprocess
import sys

OWNER = "robocode-dev"

# Folders of a results repository that hold accepted facts and the projections derived from them.
RESET_FOLDERS = ("results", "leaderboard", "matchmaking")
GENERATION_FILE = "reset-generation.json"


class EnvironmentRefused(Exception):
    """An operation was refused because it would reach the wrong environment."""


class Environment:
    def __init__(self, name, bots_repository, data_repository, resettable):
        self.name = name
        self.bots_repository = bots_repository
        self.data_repository = data_repository
        self.resettable = resettable

    @property
    def repositories(self):
        return (f"{OWNER}/{self.bots_repository}", f"{OWNER}/{self.data_repository}")


PUBLIC = Environment("public", "rumble-bots", "rumble-data", resettable=False)
TEST = Environment("test", "rumble-bots-test", "rumble-data-test", resettable=True)
ENVIRONMENTS = (PUBLIC, TEST)

_REMOTE = re.compile(
    r"^(?:https://github\.com/|git@github\.com:|ssh://git@github\.com/)"
    r"(?P<owner>[A-Za-z0-9_.-]+)/(?P<name>[A-Za-z0-9_.-]+?)(?:\.git)?/?$"
)


def repository_slug(reference):
    """Return the lower-case owner/name for a slug or GitHub remote URL, or refuse."""
    text = reference.strip()
    match = _REMOTE.match(text)
    if match is not None:
        owner, name = match.group("owner"), match.group("name")
    elif re.fullmatch(r"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", text):
        owner, name = text.split("/")
    else:
        raise EnvironmentRefused(f"Cannot tell which repository '{reference}' is.")
    return f"{owner}/{name}".lower()


def environment_of(reference):
    """The environment owning this repository, or None when it belongs to neither."""
    slug = repository_slug(reference)
    for environment in ENVIRONMENTS:
        if slug in environment.repositories:
            return environment
    return None


def require_resettable(reference):
    """Return the environment only when it is the test environment; refuse everything else."""
    environment = environment_of(reference)
    if environment is None:
        raise EnvironmentRefused(f"'{reference}' is not a Rumble repository, so it will not be reset.")
    if not environment.resettable:
        raise EnvironmentRefused(
            f"'{reference}' belongs to the {environment.name} Rumble and can never be reset.")
    return environment


def require_same_environment(expected, *references):
    """Refuse when any repository belongs to an environment other than the expected one."""
    for reference in references:
        environment = environment_of(reference)
        if environment is None or environment.name != expected.name:
            found = "no Rumble environment" if environment is None else f"the {environment.name} Rumble"
            raise EnvironmentRefused(
                f"'{reference}' belongs to {found}, but this run is for the {expected.name} Rumble.")


def _origin_of(checkout):
    result = subprocess.run(
        ["git", "-C", str(checkout), "remote", "get-url", "origin"],
        capture_output=True, text=True)
    if result.returncode != 0:
        raise EnvironmentRefused(f"'{checkout}' has no origin remote, so its environment is unknown.")
    return result.stdout.strip()


def plan_reset(checkout):
    """List what a reset of this results checkout would remove, after checking its environment."""
    checkout = Path(checkout)
    origin = _origin_of(checkout)
    environment = require_resettable(origin)
    if repository_slug(origin) != f"{OWNER}/{environment.data_repository}":
        raise EnvironmentRefused(f"'{checkout}' is not the test results repository.")
    doomed = []
    for folder in RESET_FOLDERS:
        base = checkout / folder
        if base.is_dir():
            doomed.extend(sorted(p for p in base.rglob("*") if p.is_file()))
    return doomed


def apply_reset(checkout):
    """Remove accepted facts and projections, and raise the reset generation by one.

    The generation lets the receiver reject pending submissions made before the reset.
    """
    doomed = plan_reset(checkout)
    checkout = Path(checkout)
    for path in doomed:
        path.unlink()
    marker = checkout / GENERATION_FILE
    previous = json.loads(marker.read_text(encoding="utf-8"))["generation"] if marker.exists() else 0
    marker.write_text(json.dumps({"generation": previous + 1}, indent=2) + "\n", encoding="utf-8")
    return previous + 1


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    commands = parser.add_subparsers(dest="command", required=True)
    reset = commands.add_parser("reset", help="remove accepted facts from the test results repository")
    reset.add_argument("checkout")
    reset.add_argument("--apply", action="store_true", help="do it; without this only list what would go")
    args = parser.parse_args(argv)
    try:
        if args.apply:
            print(f"Reset done. Generation is now {apply_reset(args.checkout)}.")
        else:
            doomed = plan_reset(args.checkout)
            print(f"Would remove {len(doomed)} file(s). Add --apply to do it.")
    except EnvironmentRefused as refusal:
        print(f"Refused: {refusal}", file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
