package com.patikle.swing.contents.code;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeVo {

    public CodeVo(){

    }
    public CodeVo(String keyName){
        this.keyName = keyName;
    }

    String keyName;
    String keyValue;
}
