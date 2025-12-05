package legacymodernizer.parser.service.parsing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Target별 파싱 전략을 제공하는 팩토리 클래스
 * - 전략 패턴의 Context 역할
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ParserStrategyFactory {
    
    private final PlSqlParserStrategy plSqlParserStrategy;
    private final PostgreSqlParserStrategy postgreSqlParserStrategy;
    private final JavaParserStrategy javaParserStrategy;
    
    private Map<String, TargetParserStrategy> strategyMap;
    
    /**
     * Target 타입에 따른 파싱 전략 반환
     * @param targetType Target 타입 ("oracle", "postgresql", "java" 등)
     * @return 해당 Target의 파싱 전략
     * @throws IllegalArgumentException 지원하지 않는 Target 타입인 경우
     */
    public TargetParserStrategy getStrategy(String targetType) {
        if (strategyMap == null) {
            initStrategyMap();
        }
        
        if (targetType == null || targetType.trim().isEmpty()) {
            log.warn("Target 타입이 지정되지 않았습니다. 기본값(oracle)을 사용합니다.");
            return plSqlParserStrategy;
        }
        
        String normalizedType = normalizeTargetType(targetType);
        TargetParserStrategy strategy = strategyMap.get(normalizedType);
        
        if (strategy == null) {
            log.error("지원하지 않는 Target 타입입니다: {}", targetType);
            throw new IllegalArgumentException(
                "지원하지 않는 Target 타입입니다: " + targetType + 
                ". 지원 가능한 타입: " + String.join(", ", strategyMap.keySet())
            );
        }
        
        log.debug("Target 파싱 전략 선택: {} → {}", targetType, strategy.getClass().getSimpleName());
        return strategy;
    }
    
    /**
     * 전략 맵 초기화 (폴더명과 일치하는 키 사용)
     */
    private void initStrategyMap() {
        strategyMap = new HashMap<>();
        strategyMap.put("oracle", plSqlParserStrategy);
        strategyMap.put("postgresql", postgreSqlParserStrategy);
        strategyMap.put("java", javaParserStrategy);
    }
    
    /**
     * Target 타입 정규화 (소문자 변환 및 공백 제거)
     * @param targetType 원본 Target 타입
     * @return 정규화된 Target 타입
     */
    private String normalizeTargetType(String targetType) {
        return targetType.trim().toLowerCase();
    }
    
    /**
     * 지원하는 Target 타입 목록 반환
     * @return 지원 가능한 Target 타입 목록
     */
    public List<String> getSupportedTargetTypes() {
        if (strategyMap == null) {
            initStrategyMap();
        }
        return List.copyOf(strategyMap.keySet());
    }
    
    /**
     * 새로운 전략 등록 (런타임에 전략 추가용)
     * @param targetType Target 타입
     * @param strategy 파싱 전략
     */
    public void registerStrategy(String targetType, TargetParserStrategy strategy) {
        if (strategyMap == null) {
            initStrategyMap();
        }
        strategyMap.put(normalizeTargetType(targetType), strategy);
        log.info("새 파싱 전략 등록: {} → {}", targetType, strategy.getClass().getSimpleName());
    }
}
