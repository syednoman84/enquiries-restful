package com.latidude99;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@IncludeTags("medium")
@SelectPackages("com.latidude99.layer")
@ExtendWith(SpringExtension.class)
public class TestsMedium {

}
