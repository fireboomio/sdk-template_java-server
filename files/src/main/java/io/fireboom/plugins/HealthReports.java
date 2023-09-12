package io.fireboom.plugins;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import io.fireboom.server.enums.HookParent;
import io.fireboom.server.hook.HealthReport;

import java.util.Date;

public class HealthReports {

    public static final String okStatus = "ok";
    private static final String jsonExtension = ".json";
    private static final String userDir = FileUtil.normalize(System.getProperty("user.dir"));
    public static final HealthReport report = new HealthReport();

    static {
        report.setTime(new Date());
        report.setCustomizes(CollectionUtil.newArrayList());
        report.setFunctions(CollectionUtil.newArrayList());
        report.setProxys(CollectionUtil.newArrayList());
    }

    protected static String buildReportFilepath(HookParent parent, String path) {
        return StrUtil.join(StrUtil.SLASH, userDir, parent.getValue(), path) + jsonExtension;
    }

    public static String buildRelativeClasspath(Class<?> hookClass, HookParent parent, boolean appendSimpleName) {
        String packagePath = ClassUtil.getPackagePath(hookClass);
        packagePath = StrUtil.subAfter(packagePath, parent.getValue() + StrUtil.SLASH, false);
        if (appendSimpleName) {
            packagePath = StrUtil.join(StrUtil.SLASH, packagePath, hookClass.getSimpleName());
            packagePath = StrUtil.removePrefix(packagePath, StrUtil.SLASH);
        }
        return packagePath;
    }

    protected static synchronized void reportData(HookParent parent, String path) {
        switch (parent) {
            case customize:
                report.getCustomizes().add(path);
                break;
            case function:
                report.getFunctions().add(path);
                break;
            case proxy:
                report.getProxys().add(path);
                break;
        }
    }
}
