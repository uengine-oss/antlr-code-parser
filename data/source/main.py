"""도서관 관리 시스템 - 메인 진입점
swing_system의 main.c에 대응
"""

import sys
import os

from .core.config import Config
from .core.database import Database
from .core.logger import get_logger
from .core.exceptions import LibraryError
from .services.book_service import BookService
from .services.member_service import MemberService
from .services.loan_service import LoanService
from .services.stats_service import StatsService


def print_menu():
    print()
    print("  === 도서관 관리 시스템 ===")
    print("  1. 도서 등록")
    print("  2. 도서 검색")
    print("  3. 도서 목록")
    print("  4. 회원 등록")
    print("  5. 회원 검색")
    print("  6. 도서 대출")
    print("  7. 도서 반납")
    print("  8. 대출 현황 (회원별)")
    print("  9. 연체 도서 목록")
    print("  10. 시스템 통계")
    print("  0. 종료")
    print("  선택> ", end="", flush=True)


def menu_register_book(book_svc: BookService):
    print()
    isbn = input("  ISBN: ").strip()
    title = input("  제목: ").strip()
    author = input("  저자: ").strip()
    publisher = input("  출판사: ").strip()
    year_str = input("  출판년도: ").strip()
    category = input("  분류 (일반/소설/과학/역사/기술/어린이): ").strip() or "일반"

    year = int(year_str) if year_str.isdigit() else 0
    book = book_svc.register(isbn, title, author, publisher, year, category)
    print(f"  [성공] {book.display()}")


def menu_search_book(book_svc: BookService):
    print()
    keyword = input("  검색어 (제목/저자/ISBN): ").strip()
    books = book_svc.search(keyword)
    if not books:
        print("  검색 결과가 없습니다.")
        return
    print(f"  --- 검색 결과: {len(books)}건 ---")
    for b in books:
        print(f"  {b.display()}")


def menu_list_books(book_svc: BookService):
    books = book_svc.list_all()
    if not books:
        print("  등록된 도서가 없습니다.")
        return
    print(f"\n  --- 전체 도서 목록: {len(books)}권 ---")
    for b in books:
        print(f"  {b.display()}")


def menu_register_member(member_svc: MemberService):
    print()
    name = input("  이름: ").strip()
    phone = input("  전화번호: ").strip()
    email = input("  이메일: ").strip()
    member = member_svc.register(name, phone, email)
    print(f"  [성공] {member.display()}")


def menu_search_member(member_svc: MemberService):
    print()
    keyword = input("  검색어 (이름/전화번호): ").strip()
    members = member_svc.search(keyword)
    if not members:
        print("  검색 결과가 없습니다.")
        return
    print(f"  --- 검색 결과: {len(members)}건 ---")
    for m in members:
        print(f"  {m.display()}")


def menu_borrow(loan_svc: LoanService):
    print()
    book_id = int(input("  도서 ID: ").strip())
    member_id = int(input("  회원 ID: ").strip())
    loan = loan_svc.borrow(book_id, member_id)
    print(f"  [성공] {loan.display()}")


def menu_return(loan_svc: LoanService):
    print()
    loan_id = int(input("  대출 ID: ").strip())
    loan = loan_svc.return_book(loan_id)
    print(f"  [성공] {loan.display()}")
    if loan.fine > 0:
        print(f"  연체료: {loan.fine:,}원")


def menu_member_loans(loan_svc: LoanService):
    print()
    member_id = int(input("  회원 ID: ").strip())
    loans = loan_svc.get_active_loans(member_id)
    if not loans:
        print("  현재 대출 중인 도서가 없습니다.")
        return
    print(f"  --- 대출 현황: {len(loans)}건 ---")
    for loan in loans:
        print(f"  {loan.display()}")


def menu_overdue(loan_svc: LoanService):
    loans = loan_svc.get_overdue_loans()
    if not loans:
        print("  연체 도서가 없습니다.")
        return
    print(f"\n  --- 연체 도서: {len(loans)}건 ---")
    for loan in loans:
        print(f"  {loan.display()}")


def main():
    config_file = sys.argv[1] if len(sys.argv) > 1 else "config/library.conf"
    config = Config.from_file(config_file)

    logger = get_logger("main", config.log_dir, config.log_level)

    print()
    print("  도서관 관리 시스템 v1.0")
    print()

    db = Database(config.db_path)
    db.connect()

    book_svc = BookService(db)
    member_svc = MemberService(db)
    loan_svc = LoanService(db, config)
    stats_svc = StatsService(db)

    logger.info("시스템 시작")

    try:
        while True:
            print_menu()
            try:
                choice = input().strip()
                if not choice:
                    continue

                if choice == "1":
                    menu_register_book(book_svc)
                elif choice == "2":
                    menu_search_book(book_svc)
                elif choice == "3":
                    menu_list_books(book_svc)
                elif choice == "4":
                    menu_register_member(member_svc)
                elif choice == "5":
                    menu_search_member(member_svc)
                elif choice == "6":
                    menu_borrow(loan_svc)
                elif choice == "7":
                    menu_return(loan_svc)
                elif choice == "8":
                    menu_member_loans(loan_svc)
                elif choice == "9":
                    menu_overdue(loan_svc)
                elif choice == "10":
                    stats_svc.print_summary()
                elif choice == "0":
                    break
                else:
                    print("  잘못된 선택입니다.")

            except LibraryError as e:
                print(f"  [오류] {e}")
            except ValueError:
                print("  [오류] 올바른 값을 입력하세요.")
            except EOFError:
                break

    except KeyboardInterrupt:
        print("\n  종료 신호를 받았습니다.")

    db.close()
    logger.info("시스템 종료")
    print("  도서관 관리 시스템을 종료합니다.")


if __name__ == "__main__":
    main()
