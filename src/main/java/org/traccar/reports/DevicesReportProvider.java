
package org.traccar.reports;

import jakarta.inject.Inject;
import org.jxls.util.JxlsHelper;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.helper.model.PositionUtil;
import org.traccar.model.Device;
import org.traccar.model.Message;
import org.traccar.model.User;
import org.traccar.reports.common.ReportUtils;
import org.traccar.reports.model.DeviceReportItem;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

public class DevicesReportProvider {

    private final Config config;
    private final ReportUtils reportUtils;
    private final Storage storage;

    @Inject
    public DevicesReportProvider(Config config, ReportUtils reportUtils, Storage storage) {
        this.config = config;
        this.reportUtils = reportUtils;
        this.storage = storage;
    }

    public Collection<DeviceReportItem> getObjects(long userId) throws StorageException {

        var positions = PositionUtil.getLatestPositions(storage, userId).stream()
                .collect(Collectors.toMap(Message::getDeviceId, p -> p));

        return storage.getObjects(Device.class, new Request(
                new Columns.All(),
                new Condition.Permission(User.class, userId, Device.class))).stream()
                .map(device -> new DeviceReportItem(device, positions.get(device.getId())))
                .toList();
    }

    public void getExcel(OutputStream outputStream, long userId) throws StorageException, IOException {

        File file = Paths.get(config.getString(Keys.TEMPLATES_ROOT), "export", "devices.xlsx").toFile();
        try (InputStream inputStream = new FileInputStream(file)) {
            var context = reportUtils.initializeContext(userId);
            context.putVar("items", getObjects(userId));
            JxlsHelper.getInstance().setUseFastFormulaProcessor(false)
                    .processTemplate(inputStream, outputStream, context);
        }
    }
}
