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
package com.yunjian.reportbiz.preview.export;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.yunjian.datarelation.dto.ReportCellDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author yujian
 **/
public interface Export {

    /**
     * 导出
     *
     * @param jsonObject json
     */
    void export(AtomicBoolean flag, List<ReportCellDTO> sortedCells, JSONArray styles, JSONObject cols,
                OutputStream outputStream)
            throws IOException;


}
