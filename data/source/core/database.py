"""SQLite 데이터베이스 엔진 - swing_system의 db_engine.c에 대응"""

import sqlite3
import os
from typing import Optional

from .logger import get_logger

logger = get_logger(__name__)

_CREATE_TABLES = """
CREATE TABLE IF NOT EXISTS books (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    isbn        TEXT    UNIQUE NOT NULL,
    title       TEXT    NOT NULL,
    author      TEXT    NOT NULL,
    publisher   TEXT    DEFAULT '',
    year        INTEGER DEFAULT 0,
    category    TEXT    DEFAULT '일반',
    status      TEXT    DEFAULT 'AVAILABLE',
    created_at  TEXT    DEFAULT (datetime('now','localtime'))
);

CREATE TABLE IF NOT EXISTS members (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    phone       TEXT    UNIQUE NOT NULL,
    email       TEXT    DEFAULT '',
    grade       TEXT    DEFAULT 'NORMAL',
    join_date   TEXT    DEFAULT (date('now','localtime')),
    is_active   INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS loans (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id     INTEGER NOT NULL REFERENCES books(id),
    member_id   INTEGER NOT NULL REFERENCES members(id),
    loan_date   TEXT    DEFAULT (date('now','localtime')),
    due_date    TEXT    NOT NULL,
    return_date TEXT,
    status      TEXT    DEFAULT 'ACTIVE',
    fine        INTEGER DEFAULT 0
);
"""


class Database:
    def __init__(self, db_path: str):
        os.makedirs(os.path.dirname(db_path) or ".", exist_ok=True)
        self._conn: Optional[sqlite3.Connection] = None
        self._db_path = db_path

    def connect(self) -> None:
        self._conn = sqlite3.connect(self._db_path)
        self._conn.row_factory = sqlite3.Row
        self._conn.execute("PRAGMA journal_mode=WAL")
        self._conn.execute("PRAGMA foreign_keys=ON")
        self._conn.executescript(_CREATE_TABLES)
        logger.info("데이터베이스 초기화 완료: %s", self._db_path)

    @property
    def conn(self) -> sqlite3.Connection:
        if self._conn is None:
            raise RuntimeError("데이터베이스에 연결되지 않았습니다. connect()를 먼저 호출하세요.")
        return self._conn

    def execute(self, sql: str, params: tuple = ()) -> sqlite3.Cursor:
        return self.conn.execute(sql, params)

    def fetchone(self, sql: str, params: tuple = ()) -> Optional[sqlite3.Row]:
        return self.conn.execute(sql, params).fetchone()

    def fetchall(self, sql: str, params: tuple = ()) -> list[sqlite3.Row]:
        return self.conn.execute(sql, params).fetchall()

    def commit(self) -> None:
        self.conn.commit()

    def close(self) -> None:
        if self._conn:
            self._conn.close()
            self._conn = None
            logger.info("데이터베이스 연결 종료")
