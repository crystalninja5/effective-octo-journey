/*
 * Copyright (c) 2018 Uber Technologies, Inc.
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
package testcases.E031_type_not_spreadable;

import motif.models.errors.MotifError;
import motif.models.errors.TypeNotSpreadable;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static MotifError error;

    public static void run() {
        assertThat(error).isInstanceOf(TypeNotSpreadable.class);
    }
}
