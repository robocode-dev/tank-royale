from abc import ABC

class StopResumeListenerABC(ABC):
    def on_stop(self):
        pass

    def on_resume(self):
        pass
