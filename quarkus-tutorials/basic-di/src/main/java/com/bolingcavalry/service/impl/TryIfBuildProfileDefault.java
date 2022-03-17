package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.TryIfBuildProfile;

public class TryIfBuildProfileDefault implements TryIfBuildProfile {
    @Override
    public String hello() {
        return "from " + this.getClass().getSimpleName();
    }
}
