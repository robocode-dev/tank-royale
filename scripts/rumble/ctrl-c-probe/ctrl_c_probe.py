"""Checks, on Windows, that the battle worker's shield against Ctrl+C works with a real console Ctrl+C.

Run it with `python scripts/rumble/ctrl-c-probe/ctrl_c_probe.py` and JAVA_HOME set. It starts CtrlcArgs through the same
`cmd /d /v:on /c start "" /b /wait ... & exit !errorlevel!` shield the worker host uses, with arguments that `cmd` would
otherwise read as its own (`&`, `!`, `^`, `%`, a space), from a program that catches Ctrl+C the way the Rumble command line
does. It runs twice in a console of its own: once without Ctrl+C, once with a real Ctrl+C sent to the whole console.

Both lines must show the arguments whole, `child: alive` and `cmd exit: 7`: the shielded program and its child survive,
and `cmd` waits for it and hands on its exit code.
"""
import ctypes
import os
import shutil
import subprocess
import sys
import tempfile
import time

HERE = os.path.dirname(os.path.abspath(__file__))
JAVA = os.path.join(os.environ["JAVA_HOME"], "bin", "java.exe")

if len(sys.argv) > 1 and sys.argv[1] == "launcher":
    work = sys.argv[3]
    kernel = ctypes.windll.kernel32
    # The program that started this check may have Ctrl+C ignored, which every child would inherit; it is turned back on.
    kernel.SetConsoleCtrlHandler(None, False)
    p = subprocess.Popen(f'"{JAVA}" "{os.path.join(work, "Shielded.java")}" "{work}" 7')
    time.sleep(5)
    if sys.argv[2] == "ctrlc":
        kernel.SetConsoleCtrlHandler(None, True)
        kernel.GenerateConsoleCtrlEvent(0, 0)
    time.sleep(14)
    sys.exit(0)

work = tempfile.mkdtemp(prefix="ctrl-c-probe-")
try:
    for name in ("Shielded.java", "CtrlcArgs.java"):
        shutil.copy(os.path.join(HERE, name), work)
    for mode in ("plain", "ctrlc"):
        for name in ("args.txt", "child-alive.txt", "cmd-exit.txt"):
            path = os.path.join(work, name)
            if os.path.exists(path):
                os.remove(path)
        subprocess.run([sys.executable, os.path.abspath(__file__), "launcher", mode, work], creationflags=subprocess.CREATE_NEW_CONSOLE, timeout=60)

        def read(name):
            path = os.path.join(work, name)
            return open(path).read() if os.path.exists(path) else "-"

        print(mode, "args:", read("args.txt"), "| child:", read("child-alive.txt"), "| cmd exit:", read("cmd-exit.txt"))
finally:
    shutil.rmtree(work, ignore_errors=True)
