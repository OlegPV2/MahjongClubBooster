package com.oleg.mahjongclubbooster.userservice;

import com.oleg.mahjongclubbooster.bean.BeanFile;

interface IFileExplorerService {
    List<BeanFile> listFiles(String path);
}