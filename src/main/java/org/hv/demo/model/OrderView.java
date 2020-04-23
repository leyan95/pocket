package org.hv.demo.model;

import org.hv.pocket.annotation.Column;
import org.hv.pocket.annotation.View;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author wujianchuan
 */
@View
public class OrderView implements Serializable {

    private static final long serialVersionUID = 2802482894392769141L;
    @Column
    private String code;
    @Column
    private BigDecimal price;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
