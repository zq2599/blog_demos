package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.TryIfBuildProfile;

public class TryIfBuildProfileProd implements TryIfBuildProfile {
    @Override
    public String hello() {
        return "from " + this.getClass().getSimpleName();
    }
}
