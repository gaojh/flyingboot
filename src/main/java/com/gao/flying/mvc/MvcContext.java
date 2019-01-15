package com.gao.flying.mvc;

import com.gao.flying.mvc.http.FlyingRequest;
import com.gao.flying.mvc.http.FlyingResponse;
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

    private FlyingRequest flyingRequest;
    private FlyingResponse flyingResponse;

}
