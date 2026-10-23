package com.mr.vo;

import lombok.Data;

@Data
public class CredentialVO {
    // 入户方式：SMART_LOCK/PROPERTY/KEY_DEPOSIT/AT_HOME
    private String entryMode;
    // 凭证明文（AT_HOME 时为 null）
    private String secret;
    // 友好提示（比如「主人在家，无需凭证」）
    private String tip;
}
