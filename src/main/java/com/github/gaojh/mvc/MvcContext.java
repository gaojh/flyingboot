package com.github.gaojh.mvc;

import com.github.gaojh.mvc.http.HttpRequest;
import com.github.gaojh.mvc.http.HttpResponse;
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
