package ydsu.module.util.source

import com.google.common.base.Preconditions
import com.google.common.base.Strings

@Grab(group = 'com.google.guava', module = 'guava', version = '28.1-jre', transitive = false)
class Args {
    static notBlank(String argName, String argValue) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(argValue) && !argValue.trim().isEmpty(),
                "Argument '$argName' must not be blank")
    }
}
