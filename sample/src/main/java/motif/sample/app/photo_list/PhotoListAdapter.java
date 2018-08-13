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
package motif.sample.app.photo_list;


import android.view.ViewGroup;

import motif.sample.app.photo_list_item.PhotoListItemView;
import motif.sample.lib.controller.Controller;
import motif.sample.lib.controller.ControllerAdapter;
import motif.sample.lib.db.Photo;
import motif.sample.lib.photo.PhotoDiffItemCallback;

public class PhotoListAdapter extends ControllerAdapter<Photo, PhotoListItemView> {

    public PhotoListAdapter(PhotoListScope scope) {
        super(new Factory<Photo, PhotoListItemView>() {
            @Override
            public Controller controller(PhotoListItemView view, Photo item) {
                return scope.item(view, item).controller();
            }

            @Override
            public PhotoListItemView view(ViewGroup parent) {
                return PhotoListItemView.create(parent);
            }
        }, new PhotoDiffItemCallback());
    }
}
