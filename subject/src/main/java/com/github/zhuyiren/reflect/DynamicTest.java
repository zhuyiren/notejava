/*
 * Copyright 2017 The ZRPC Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.zhuyiren.reflect;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Proxy;

/**
 * @author zhuyiren
 * @date 2017/9/23
 */
public class DynamicTest {


    @State(Scope.Benchmark)
    private static class TargetState {
        final static TargetImpl TARGET_IMPL = new TargetImpl();
        final static Target JDK_DYNAMIC_TARGET = (Target) Proxy.newProxyInstance(Target.class.getClassLoader(), new Class[]{Target.class}, (proxy, method, args) -> method.invoke(TargetState.TARGET_IMPL, args));

        final static Target CGLIB_DYNAMIC_TARGET = (Target) Enhancer.create(Target.class,
                (InvocationHandler) (proxy, method, args) -> method.invoke(TARGET_IMPL, args));

        final static TargetImpl CGLIB_DYNAMIC_TARGETIMPL = (TargetImpl) Enhancer.create(TargetImpl.class,
                (InvocationHandler) (proxy, method, args) -> method.invoke(TARGET_IMPL, args));

    }


    /**
     * <p>
     * Result "com.github.zhuyiren.reflect.DynamicTest.jdkDynamic":<br>
     * 387457392.980 ±(99.9%) 10579533.079 ops/s [Average]<br>
     * (min, avg, max) = (343851649.181, 387457392.980, 400385054.532), stdev = 14123373.978<br>
     * CI (99.9%): [376877859.901, 398036926.060] (assumes normal distribution)<br>
     * <br>
     * # Run complete. Total time: 00:00:51<br>
     * <br>
     * Benchmark                Mode  Cnt          Score          Error  Units<br>
     * DynamicTest.jdkDynamic  thrpt   25  387457392.980 ± 10579533.079  ops/s<br>
     * </p>
     */
    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    @Fork(value = 5)
    public void jdkDynamic() {
        TargetState.JDK_DYNAMIC_TARGET.getName();
    }

    /**
     * Result "com.github.zhuyiren.reflect.DynamicTest.cglibDynamic":<br>
     * 270780634.506 ±(99.9%) 1496215.113 ops/s [Average]<br>
     * (min, avg, max) = (264863936.730, 270780634.506, 274140096.284), stdev = 1997404.369<br>
     * CI (99.9%): [269284419.393, 272276849.619] (assumes normal distribution)<br>
     * <br>
     * <br>
     * # Run complete. Total time: 00:00:52<br>
     * <br>
     * Benchmark                  Mode  Cnt          Score         Error  Units<br>
     * DynamicTest.cglibDynamic  thrpt   25  270780634.506 ± 1496215.113  ops/s
     */
    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    @Fork(value = 5)
    public void cglibDynamic() {
        TargetState.CGLIB_DYNAMIC_TARGET.getName();
    }


    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    @Fork(value = 5)
    public void cglibDynamicImpl() {
        TargetState.CGLIB_DYNAMIC_TARGETIMPL.getName();
    }


    /**
     * Result "com.github.zhuyiren.reflect.DynamicTest.direct":<br>
     * 3311999057.141 ±(99.9%) 69784140.394 ops/s [Average]<br>
     * (min, avg, max) = (3055599463.526, 3311999057.141, 3387021489.317), stdev = 93159830.884<br>
     * CI (99.9%): [3242214916.747, 3381783197.535] (assumes normal distribution)<br>
     * <p>
     * <p>
     * # Run complete. Total time: 00:00:51<br>
     * <p>
     * Benchmark            Mode  Cnt           Score          Error  Units<br>
     * DynamicTest.direct  thrpt   25  3311999057.141 ± 69784140.394  ops/s
     */
    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    @Fork(value = 5)
    public void direct() {
        TargetState.TARGET_IMPL.getName();
    }

}