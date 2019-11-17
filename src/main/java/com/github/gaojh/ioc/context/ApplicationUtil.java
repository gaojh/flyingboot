package com.github.gaojh.ioc.context;

import com.github.gaojh.mvc.context.WebContext;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 高建华
 * @date 2019-03-31 23:15
 */
public final class ApplicationUtil {

    @Setter
    @Getter
    public static WebContext webContext;


    @Setter
    @Getter
    public static ApplicationContext applicationContext;
}
