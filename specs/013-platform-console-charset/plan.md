# Implementation Plan: Platform-Aware Console Charset

**Branch**: `main` | **Date**: 2026-07-24 | **Spec**: [spec.md](spec.md)

## Summary

`logback-spring.xml`의 콘솔 기본값 `UTF-8`을 JVM `file.encoding` 기반으로 바꾼다. Spring의
`logging.charset.console` 명시 설정은 계속 최우선이며 파일/JSON UTF-8 계약은 변경하지 않는다.

## Technical Context

**Language/Version**: Java 17

**Primary Dependencies**: Spring Boot 3.3, Logback

**Storage**: UTF-8 AST/diagnostic/repair JSON

**Testing**: Maven Surefire, `FullCorpusRecoveryTest`

**Target Platform**: Windows MS949 및 UTF-8 실행 환경

**Project Type**: Spring Boot parser service

**Constraints**: 콘솔 외 직렬화 바이트 변경 금지, 고객별 분기 금지

**Scale/Scope**: 설정 파일 1개와 회귀 테스트

## Constitution Check

- 현재 코드와 실행 결과로 MS949 JVM + UTF-8 콘솔 appender 충돌을 확인했다.
- 환경 문자셋을 단일 기본값으로 사용하며 고객별 예외를 만들지 않는다.
- 원본→AST JSON의 UTF-8 계약은 독립 검증한다.

## Current and Target Flow

```text
현재: 한국어 String → Logback UTF-8 encoder → MS949 Surefire capture → 모지바케
목표: 한국어 String → 명시 charset 또는 JVM console charset → capture → 원문

파일: 한국어 String → UTF-8 JSON writer → strict UTF-8 JSON (변경 없음)
```

## Project Structure

```text
src/main/resources/logback-spring.xml
specs/013-platform-console-charset/
```

**Structure Decision**: 기존 Logback 설정의 책임 안에서 기본값만 바로잡는다.
