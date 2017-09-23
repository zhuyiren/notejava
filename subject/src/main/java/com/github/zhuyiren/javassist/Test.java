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

package com.github.zhuyiren.javassist;

import com.github.zhuyiren.reflect.Target;
import com.github.zhuyiren.reflect.TargetImpl;
import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * @author zhuyiren
 * @date 2017/9/23
 */
public class Test {


    public static void main(String[] args) throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.makeClass("test");
        cc.addInterface(cp.get(Target.class.getCanonicalName()));
        CtClass targetCc = cp.get(Target.class.getCanonicalName());
        CtField field = CtField.make("private int impl;",cc);
        cc.addField(field);
        CtConstructor constructor=new CtConstructor(new CtClass[]{targetCc},cc);
        constructor.setBody("{" +
                "this.impl=$0;}");
        cc.addConstructor(constructor);
        Class t = cc.toClass();

        Constructor c = t.getConstructor(Target.class);
        Target proxy = (Target) c.newInstance(1);

    }
}