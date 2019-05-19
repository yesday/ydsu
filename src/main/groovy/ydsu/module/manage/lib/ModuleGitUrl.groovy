package ydsu.module.manage.lib

import groovy.transform.ToString

@ToString
class ModuleGitUrl {
    String fullUrl // https://github.com/yesday/ydsu-module/master/src/main/groovy/ydsu/module/skeleton
    String owner // yesday
    String repository // ydsu-module
    String httpsCloneUrl // https://github.com/yesday/ydsu-module.git
    String branch // master
    String modulePath // src/main/groovy/ydsu/module/skeleton

    static ModuleGitUrl newInstance(String fullUrl) {
        ModuleGitUrl url = new ModuleGitUrl()
        url.fullUrl = fullUrl
        int begin = 19
        int end = fullUrl.indexOf('/', begin)
        url.owner = fullUrl.substring(begin, end)
        begin = end + 1
        end = fullUrl.indexOf('/', begin)
        url.repository = fullUrl.substring(begin, end)
        url.httpsCloneUrl = "https://github.com/${url.owner}/${url.repository}.git"
        begin = end + 1
        end = fullUrl.indexOf('/', begin)
        url.branch = fullUrl.substring(begin, end)
        begin = end + 1
        url.modulePath = fullUrl.substring(begin)
        url
    }
}
