/*
 * Copyright 2025-2030 大连云建数科科技有限公司.
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
package com.yunjian.reportbiz.preview.export.pdf;

import com.itextpdf.io.font.FontProgramFactory;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

@Slf4j
/**
 * @author yujian
 **/
public class FontFileFactory {

    /**
     * 这里字体是从UREPORT拿过来的 有些问题 比如SIMHEI这个字体 会导致文件非常大
     */

    static final String[] fontNames = {"ARIAL.TTF", "COMIC.TTF", "COUR.TTF", "IMPACT.TTF",
            "SIMFANG.TTF", "SIMHEI.TTF", "SIMKAI.TTF", "TIMES.TTF"};


    public static void init() {
        try {
            initCacheFont();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void initCacheFont() throws IOException {
        for (String fontName : fontNames) {
            File file = ResourceUtils.getFile("fonts/" + fontName);
            FontProgramFactory.registerFont(file.getPath(), fontName.split("\\.")[0]);
        }


    }


}
