package com.gao.flying.example.ctrl;

import lombok.Data;

/**
 * @author 高建华
 * @date 2019-01-02 20:54
 */
@Data
public class  CommonUser<T> {
    T user;

    String age;


}
