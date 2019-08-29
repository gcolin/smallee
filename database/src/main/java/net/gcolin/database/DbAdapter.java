/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.gcolin.database;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 */
public class DbAdapter {

    public static final BiFunction<Double, Double, String> GEOGRAPHY = (x, y) -> {
        DecimalFormat df = new DecimalFormat("###.##");
        return "st_GeographyFromText('SRID=4326;POINT(" + df.format(x) + " " + df.format(y) + ")')";
    };

    public static final BiFunction<Double, Double, String> GEOMETRY = (x, y) -> {
        DecimalFormat df = new DecimalFormat("###.##");
        return "ST_GeomFromText('POINT(" + df.format(x) + " " + df.format(y) + "), 27572')";
    };

    public static final DbAdapter DERBY = new DbAdapter(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
            "values (NEXT VALUE FOR %s)", " FETCH FIRST ROW ONLY", false, null);
    public static final DbAdapter H2 = new DbAdapter(" limit ? offset ?", "select nextval('%s')", " limit 1", true,
            GEOMETRY);
    public static final DbAdapter POSTGRES = new DbAdapter(" offset ? limit ?", "select nextval('%s')", " limit 1",
            false, GEOGRAPHY);
    public static final Map<String, DbAdapter> ALL;

    static {
        Map<String, DbAdapter> map = new HashMap<>();
        map.put("h2", H2);
        map.put("postgres", POSTGRES);
        map.put("derby", DERBY);
        ALL = Collections.unmodifiableMap(map);
    }

    public String offsetlimit;
    public String limit1;
    public String nextVal;
    public int offsetIdx = 0;
    public int limitIdx = 1;
    public BiFunction<Double, Double, String> toPoint;

    public DbAdapter(String offsetlimit, String nextVal, String limit1, boolean reverse,
            BiFunction<Double, Double, String> toPoint) {
        this.offsetlimit = offsetlimit;
        this.nextVal = nextVal;
        this.limit1 = limit1;
        this.toPoint = toPoint;
        if (reverse) {
            this.offsetIdx = 1;
            this.limitIdx = 0;
        }
    }

}
