package com.github.gaojh.mvc.route;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 高建华
 * @date 2019-05-10 11:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouterFunction {
    private List<Route> routeList;
}
