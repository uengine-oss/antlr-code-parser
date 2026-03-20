"""로깅 모듈 - swing_system의 log.c에 대응"""

import logging
import os
from datetime import datetime


_LOG_FORMAT = "[%(asctime)s] %(levelname)-5s %(name)s - %(message)s"
_DATE_FORMAT = "%Y-%m-%d %H:%M:%S"
_initialized = False


def get_logger(name: str, log_dir: str = "./logs", level: str = "INFO") -> logging.Logger:
    global _initialized
    logger = logging.getLogger(name)

    if not _initialized:
        os.makedirs(log_dir, exist_ok=True)

        log_file = os.path.join(
            log_dir, f"library_{datetime.now():%Y%m%d}.log"
        )

        file_handler = logging.FileHandler(log_file, encoding="utf-8")
        file_handler.setFormatter(logging.Formatter(_LOG_FORMAT, _DATE_FORMAT))

        console_handler = logging.StreamHandler()
        console_handler.setFormatter(logging.Formatter(_LOG_FORMAT, _DATE_FORMAT))

        root = logging.getLogger()
        root.setLevel(getattr(logging, level.upper(), logging.INFO))
        root.addHandler(file_handler)
        root.addHandler(console_handler)
        _initialized = True

    return logger
