package com.mr.service.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mr.common.exception.BaseException;
import com.mr.entity.Pet;
import com.mr.entity.Service;
import com.mr.entity.ServicePriceRule;
import com.mr.vo.PricePreviewVO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 价格计算器：试算接口与下单共用，保证所见即所付
 * 计价口径：所有"按次"规则都乘以天数
 */
@Component
public class PriceCalculator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PricePreviewVO calculate(Service service, List<ServicePriceRule> rules,
                                    List<Pet> pets, int days, BigDecimal distanceKm) {
        //基础费 = 起步价 × 天数
        int base = service.getPriceFrom() * days;
        int extra = 0;

        List<PricePreviewVO.FeeItem> items = new ArrayList<>();
        items.add(new PricePreviewVO.FeeItem("基础服务费（" + days + "天）", base));

        //所有计价规则取宠物中的最大体重（洗护按最重的那只计费）
        BigDecimal maxWeight = pets.stream()
                .map(Pet::getWeightKg)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(null);

        for (ServicePriceRule rule : rules) {
            JsonNode cfg = readConfig(rule.getRuleConfig());
            int fee = 0;

            switch (rule.getRuleType()) {
                case "EXTRA_PET": {
                    int freeCount = cfg.path("freeCount").asInt(1);
                    int extraPets = Math.max(0, pets.size() - freeCount);
                    fee = extraPets * rule.getAmount() * days;
                    break;
                }
                case "WEIGHT": {
                    if (maxWeight == null) {
                        throw new BaseException("请先完善宠物档案中的体重");
                    }
                    BigDecimal freeKg = BigDecimal.valueOf(cfg.path("freeKg").asInt());
                    BigDecimal stepKg = BigDecimal.valueOf(cfg.path("stepKg").asInt(1));
                    BigDecimal over = maxWeight.subtract(freeKg);
                    if (over.compareTo(BigDecimal.ZERO) > 0) {
                        long steps = (long) Math.ceil(
                                over.divide(stepKg, 2, RoundingMode.HALF_UP).doubleValue());
                        fee = (int) (steps * rule.getAmount() * days);
                    }
                    break;
                }
                case "DISTANCE": {
                    if (distanceKm != null) {
                        BigDecimal freeKm = BigDecimal.valueOf(cfg.path("freeKm").asInt());
                        BigDecimal over = distanceKm.subtract(freeKm);
                        if (over.compareTo(BigDecimal.ZERO) > 0) {
                            long km = (long) Math.ceil(over.doubleValue()); //超距向上取整
                            fee = (int) (km * rule.getAmount() * days);
                        }
                    }
                    break;
                }
                default:
                    break;
            }

            if (fee > 0) {
                extra += fee;
                items.add(new PricePreviewVO.FeeItem(rule.getLabel(), fee));
            }
        }

        PricePreviewVO vo = new PricePreviewVO();
        vo.setBaseAmount(base);
        vo.setExtraAmount(extra);
        vo.setDiscountAmount(0);
        vo.setTotalAmount(base + extra);
        vo.setItems(items);
        return vo;
    }

    private JsonNode readConfig(String json) {
        try {
            return json == null || json.isEmpty()
                    ? objectMapper.createObjectNode()
                    : objectMapper.readTree(json);
        } catch (Exception e) {
            throw new BaseException("计价规则配置错误");
        }
    }
}