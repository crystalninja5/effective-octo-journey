package com.uber.motif.sample.filerow;

import io.reactivex.Observable;

import java.io.File;

public interface FileTouches {

    Observable<File> touches();
}
