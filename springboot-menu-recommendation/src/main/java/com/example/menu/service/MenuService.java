package com.example.menu.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 메뉴 추천 비즈니스 로직을 담당하는 Spring Bean입니다.
 *
 * @Service를 사용하면 Component Scan을 통해 Spring Container에
 * 자동으로 Bean으로 등록됩니다.
 */
@Service
public class MenuService {

    private final List<String> menus = List.of(
            "김치찌개",
            "불고기",
            "짜장면",
            "돈가스",
            "떡볶이",
            "치킨",
            "피자"
    );

    public String recommend() {
        return "김치찌개";
    }

    public String recommendByCategory(String category) {
        return switch (category) {
            case "korean" -> "불고기";
            case "chinese" -> "짜장면";
            case "japanese" -> "돈가스";
            case "snack" -> "떡볶이";
            default -> "추천 가능한 메뉴가 없습니다";
        };
    }

    public String recommendByWeather(String whether) {
        return switch (whether) {
            case "sunny" -> "타코 플래터";
            case "rainy" -> "해물파전";
            case "hot" -> "냉면";
            case "cold" -> "우동";
            default -> "추천 가능한 메뉴가 없습니다";
        };
    }

    public String randomMenu() {
        int index = ThreadLocalRandom.current().nextInt(menus.size());
        return menus.get(index);
    }

    public String recommendByMood(String mood) {
        return switch (mood) {
            case "happy" -> "치킨";
            case "sad" -> "떡볶이";
            case "tired" -> "삼계탕";
            case "stressed" -> "매운 마라탕";
            default -> "추천 가능한 메뉴가 없습니다";
        };
    }

    public String recommendByPrice(int min, int max) {
        if (min > max) {
            return "가격 범위가 올바르지 않습니다";
        }
        if (max <= 6000) {
            return "김밥";
        } else if (max <= 12000) {
            return "돈가스";
        } else {
            return "스테이크 정식";
        }
    }

    public String recommendForMe(String companion) {
        return switch (companion) {
            case "solo" -> "편의점 도시락";
            case "friends" -> "치킨과 맥주";
            case "family" -> "삼겹살 파티";
            default -> "추천 가능한 메뉴가 없습니다";
        };
    }
}
