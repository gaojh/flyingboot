package com.github.flying.mvc;

import com.github.flying.mvc.http.HttpRequest;
import com.github.flying.mvc.http.HttpResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 高建华
 * @date 2019-01-06 21:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MvcContext {

    private HttpRequest httpRequest;
    private HttpResponse httpResponse;

}
