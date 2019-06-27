package com.latidude99;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@IncludeTags("slow")
@SelectPackages("com.latidude99.integration.rest")
@ExtendWith(SpringExtension.class)
public class TestsSlow {

}
