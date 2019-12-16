package com.github.gaojh.starter;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.github.gaojh.ioc.context.AppContext;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author gaojianhua
 * @date 2019/12/13 11:22 上午
 */
@Slf4j
public class StarterContext {

    private static Set<String> starters = new HashSet<>();

    private AppContext appContext;

    public StarterContext(AppContext appContext){
         this.appContext = appContext;
    }

    public void scanStarter() throws Exception {
        Enumeration<URL> en = ClassLoaderUtil.getClassLoader().getResources("META-INF/flying.starter");
        while (en.hasMoreElements()) {
            URL url = en.nextElement();
            log.debug("Found " + url);
            try (InputStream ins = url.openStream()) {
                InputStreamReader reader = new InputStreamReader(ins);
                String tmp = IoUtil.read(reader);
                if (!StrUtil.isBlank(tmp)) {
                    for (String t : StrUtil.split(tmp, "[\n]")) {
                        String className = t.trim();
                        if (!starters.add(className)) {
                            continue;
                        }
                        Class<?> klass = ClassUtil.loadClass(className);
                        appContext.createBeanDefine(klass);
                    }
                }
            }
        }
    }
}
