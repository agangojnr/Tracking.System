//
//package org.traccar.reports;
//
//import jakarta.inject.Inject;
//import org.apache.poi.ss.util.WorkbookUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.traccar.config.Config;
//import org.traccar.config.Keys;
//import org.traccar.helper.model.DeviceUtil;
//import org.traccar.model.*;
//import org.traccar.reports.common.ReportUtils;
//import org.traccar.reports.model.DeviceReportSection;
//import org.traccar.reports.model.OfflineReportItem;
//import org.traccar.reports.model.StopReportItem;
//import org.traccar.storage.Storage;
//import org.traccar.storage.StorageException;
//import org.traccar.storage.query.Columns;
//import org.traccar.storage.query.Condition;
//import org.traccar.storage.query.Request;
//
//import java.io.*;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Date;
//
//public class OfflineReportProvider {
//
//    private final Config config;
//    private final ReportUtils reportUtils;
//    private final Storage storage;
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(OfflineReportItem.class);
//
//    @Inject
//    public OfflineReportProvider(Config config, ReportUtils reportUtils, Storage storage) {
//        this.config = config;
//        this.reportUtils = reportUtils;
//        this.storage = storage;
//    }
//
//    public Collection<OfflineReportItem> getResellerObjects(
//            long resellerid, Date from, Date to) throws StorageException {
//        //reportUtils.checkPeriodLimit(from, to);
//        LOGGER.info("Testing the offline check.... Datefrom - {}   Dateto - {}  Resellerid - {}", from, to, resellerid);
//
//        ArrayList<OfflineReportItem> result = new ArrayList<>();
//        Collection<OfflineReportItem> devices = storage.getJointObjects(OfflineReportItem.class, new Request(
//                new Columns.All(),
////                new Columns.Include(
////                        "subresellerName AS subresellerName",
////                        "clientname AS clientName",
////                        "name AS assetName",
////                        "uniqueid AS imei",
////                        "deviceType AS deviceModel",
////                        "simcard AS simcardNo",
////                        "simcardType AS simcardType"
////                ),
//
//        new Condition.getResellerOfflineDevice(Device.class, "id", ClientDevice.class,"clientid", "deviceid", SubresellerClient.class, "subresellerid", "clientid", ResellerSubreseller.class, "resellerid","subresellerid", DeviceAsset.class,"deviceid", "resellerid",resellerid, "status","offline")));
//        //return devices;
//        for (OfflineReportItem device: devices){
//            OfflineReportItem item = new OfflineReportItem();
//            item.setResellerName(device.getResellerName());
//            item.setSubresellerName(device.getSubresellerName());
//            item.setClientName(device.getClientName());
//            item.setAssetName(device.getAssetName());
//            item.setImei(device.getImei());
//            item.setDeviceType(device.getDeviceType());
//            item.setSimcard(device.getSimcard());
//            item.setSimcardType(device.getSimcardType());
//            //result.addAll(item);
//        }
//
//        return devices;
//    }
//
//    public void getExcel(
//            OutputStream outputStream, long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
//            Date from, Date to) throws StorageException, IOException {
//        reportUtils.checkPeriodLimit(from, to);
//
//        ArrayList<DeviceReportSection> devicesStops = new ArrayList<>();
//        ArrayList<String> sheetNames = new ArrayList<>();
//        for (Device device: DeviceUtil.getAccessibleDevices(storage, userId, deviceIds, groupIds)) {
//            Collection<StopReportItem> stops = reportUtils.detectTripsAndStops(device, from, to, StopReportItem.class);
//            DeviceReportSection deviceStops = new DeviceReportSection();
//            deviceStops.setDeviceName(device.getName());
//            sheetNames.add(WorkbookUtil.createSafeSheetName(deviceStops.getDeviceName()));
//            if (device.getGroupId() > 0) {
//                Group group = storage.getObject(Group.class, new Request(
//                        new Columns.All(), new Condition.Equals("id", device.getGroupId())));
//                if (group != null) {
//                    deviceStops.setGroupName(group.getName());
//                }
//            }
//            deviceStops.setObjects(stops);
//            devicesStops.add(deviceStops);
//        }
//
//        File file = Paths.get(config.getString(Keys.TEMPLATES_ROOT), "export", "stops.xlsx").toFile();
//        try (InputStream inputStream = new FileInputStream(file)) {
//            var context = reportUtils.initializeContext(userId);
//            context.putVar("devices", devicesStops);
//            context.putVar("sheetNames", sheetNames);
//            context.putVar("from", from);
//            context.putVar("to", to);
//            reportUtils.processTemplateWithSheets(inputStream, outputStream, context);
//        }
//    }
//
//}


package org.traccar.reports;

import jakarta.inject.Inject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.model.*;
import org.traccar.reports.common.ReportUtils;
import org.traccar.reports.model.OfflineReportItem;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

public class OfflineReportProvider {
    private final Config config;
    private final ReportUtils reportUtils;
    private final Storage storage;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(OfflineReportProvider.class);

    @Inject
    public OfflineReportProvider(Config config, ReportUtils reportUtils, Storage storage) {
        this.config = config;
        this.reportUtils = reportUtils;
        this.storage = storage;
    }

    // ==========================================================
    // JSON REPORT
    // ==========================================================
    public Collection<OfflineReportItem> getResellerOfflineReport(
            long resellerId)
            throws StorageException {

        ArrayList<OfflineReportItem> result = new ArrayList<>();

        Collection<OfflineReportItem> devices =
                storage.getJointObjects(
                        OfflineReportItem.class,
                        new Request(
                                new Columns.Include(
                                        "tc_resellers.resellername AS resellerName",
                                        "tc_subresellers.subresellername AS SubresellerName",
                                        "tc_clients.clientname AS clientName",
                                        "tc_devices.name AS assetName",
                                        "tc_devices.uniqueid AS imei",
                                        "tc_simcards.phonenumber AS simcard",
                                        "tc_devicetypes.model AS deviceType",
                                        "tc_devices.status AS status",
                                        "tc_devices.lastupdate AS lastReportDate",
                                        "DATEDIFF(DAY, tc_devices.lastupdate, GETDATE()) AS daysoffline"
                                        //"tc_networkproviders.networkprovidername AS simcardType"
                                ),
                                new Condition.getResellerOfflineDevice(
                                        Device.class, "id","devicetypeid",
                                        ClientDevice.class, "clientid", "deviceid",
                                        Client.class, "id",
                                        SubresellerClient.class, "subresellerid", "clientid",
                                        Subreseller.class, "id",
                                        ResellerSubreseller.class, "resellerid", "subresellerid",
                                        Reseller.class, "id",
                                        AssetDevice.class, "deviceid",
                                        Devicetype.class, "id",
                                        DeviceSimcard.class, "deviceid","simcardid",
                                        Simcard.class, "id","networkproviderid",
                                        Networkprovider.class, "id",
                                        "resellerid", resellerId,
                                        "status", "offline","not_connected",
                                         "lastupdate"
                                )
                        ));

        // map result safely
        for (OfflineReportItem device : devices) {

            OfflineReportItem item = new OfflineReportItem();

            item.setResellerName(device.getResellerName());
            item.setSubresellerName(device.getSubresellerName());
            item.setClientName(device.getClientName());
            item.setAssetName(device.getAssetName());
            item.setImei(device.getImei());
            item.setDeviceType(device.getDeviceType());
            item.setSimcard(device.getSimcard());
            item.setStatus(device.getStatus());
            item.setLastReportDate(device.getLastReportDate());
            item.setDaysoffline(device.getDaysOffline());
            result.add(item);
        }
        return result;
    }

    /* GETTING OFFLINE PER SUBRESELLER */
    public Collection<OfflineReportItem> getSubresellerOfflineReport(
            long subresellerId)
            throws StorageException {

        ArrayList<OfflineReportItem> result = new ArrayList<>();

        Collection<OfflineReportItem> devices =
                storage.getJointObjects(
                        OfflineReportItem.class,
                        new Request(
                                new Columns.Include(
                                        "tc_subresellers.subresellername AS SubresellerName",
                                        "tc_clients.clientname AS clientName",
                                        "tc_devices.name AS assetName",
                                        "tc_devices.uniqueid AS imei",
                                        "tc_simcards.phonenumber AS simcard",
                                        "tc_devicetypes.model AS deviceType",
                                        "tc_devices.status AS status",
                                        "tc_devices.lastupdate AS lastReportDate",
                                        "DATEDIFF(DAY, tc_devices.lastupdate, GETDATE()) AS daysoffline"
                                        //"tc_networkproviders.networkprovidername AS simcardType"
                                ),
                                new Condition.getSubresellerOfflineDevice(
                                        Device.class, "id","devicetypeid",
                                        ClientDevice.class, "clientid", "deviceid",
                                        Client.class, "id",
                                        SubresellerClient.class, "subresellerid", "clientid",
                                        Subreseller.class, "id",
//                                        ResellerSubreseller.class, "resellerid", "subresellerid",
//                                        Reseller.class, "id",
                                        AssetDevice.class, "deviceid",
                                        Devicetype.class, "id",
                                        DeviceSimcard.class, "deviceid","simcardid",
                                        Simcard.class, "id","networkproviderid",
                                        Networkprovider.class, "id",
                                        "subresellerid", subresellerId,
                                        "status", "offline","not_connected",
                                         "lastupdate"
                                )
                        ));

        // map result safely
        for (OfflineReportItem device : devices) {

            OfflineReportItem item = new OfflineReportItem();

            item.setResellerName(device.getResellerName());
            item.setSubresellerName(device.getSubresellerName());
            item.setClientName(device.getClientName());
            item.setAssetName(device.getAssetName());
            item.setImei(device.getImei());
            item.setDeviceType(device.getDeviceType());
            item.setSimcard(device.getSimcard());
            item.setStatus(device.getStatus());
            item.setLastReportDate(device.getLastReportDate());
            item.setDaysoffline(device.getDaysOffline());
            result.add(item);
        }
        return result;
    }

    /* GETTING OFFLINE PER CLIENT */
    public Collection<OfflineReportItem> getClientOfflineReport(
            long clientId)
            throws StorageException {

        ArrayList<OfflineReportItem> result = new ArrayList<>();

        Collection<OfflineReportItem> devices =
                storage.getJointObjects(
                        OfflineReportItem.class,
                        new Request(
                                new Columns.Include(
                                        "tc_clients.clientname AS clientName",
                                        "tc_devices.name AS assetName",
                                        "tc_devices.uniqueid AS imei",
                                        "tc_simcards.phonenumber AS simcard",
                                        "tc_devicetypes.model AS deviceType",
                                        "tc_devices.status AS status",
                                        "tc_devices.lastupdate AS lastReportDate",
                                        "DATEDIFF(DAY, tc_devices.lastupdate, GETDATE()) AS daysoffline"
                                        //"tc_networkproviders.networkprovidername AS simcardType"
                                ),
                                new Condition.getClientOfflineDevice(
                                        Device.class, "id","devicetypeid",
                                        ClientDevice.class, "clientid", "deviceid",
                                        Client.class, "id",
//                                        SubresellerClient.class, "subresellerid", "clientid",
//                                        Subreseller.class, "id",
//                                        ResellerSubreseller.class, "resellerid", "subresellerid",
//                                        Reseller.class, "id",
                                        AssetDevice.class, "deviceid",
                                        Devicetype.class, "id",
                                        DeviceSimcard.class, "deviceid","simcardid",
                                        Simcard.class, "id","networkproviderid",
                                        Networkprovider.class, "id",
                                        "clientid", clientId,
                                        "status", "offline","not_connected",
                                        "lastupdate"
                                )
                        ));

        // map result safely
        for (OfflineReportItem device : devices) {

            OfflineReportItem item = new OfflineReportItem();

            item.setResellerName(device.getResellerName());
            item.setSubresellerName(device.getSubresellerName());
            item.setClientName(device.getClientName());
            item.setAssetName(device.getAssetName());
            item.setImei(device.getImei());
            item.setDeviceType(device.getDeviceType());
            item.setSimcard(device.getSimcard());
            item.setStatus(device.getStatus());
            item.setLastReportDate(device.getLastReportDate());
            item.setDaysoffline(device.getDaysOffline());

            result.add(item);
        }
        return result;
    }


    // ==========================================================
    // EXCEL REPORT
    // ==========================================================
    public void exportOfflineExcel(
            OutputStream outputStream,
            long resellerId,
            String from,
            String to)
            throws StorageException, IOException {

        Collection<OfflineReportItem> data =
                getResellerOfflineReport(resellerId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Offline Devices");

        // Header
        Row header = sheet.createRow(0);

        String[] columns = {
                "Reseller",
                "Subreseller",
                "Client",
                "Asset",
                "IMEI",
                "Device Type",
                "Simcard"
        };

        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
        }

        // Data rows
        int rowIndex = 1;
        for (OfflineReportItem item : data) {

            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(item.getResellerName());
            row.createCell(1).setCellValue(item.getSubresellerName());
            row.createCell(2).setCellValue(item.getClientName());
            row.createCell(3).setCellValue(item.getAssetName());
            row.createCell(4).setCellValue(item.getImei());
            row.createCell(5).setCellValue(item.getDeviceType());
            row.createCell(6).setCellValue(item.getSimcard());
        }

        // autosize columns
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(outputStream);
        workbook.close();
    }

    public void getExcel(OutputStream outputStream, long resellerId, String from, String to)
            throws StorageException, IOException {

        // Load offline template instead of devices.xlsx
        File file = Paths.get(
                config.getString(Keys.TEMPLATES_ROOT),
                "export",
                "offline.xlsx"   // changed template
        ).toFile();

        try (InputStream inputStream = new FileInputStream(file)) {

            var context = reportUtils.initializeContext(resellerId);

            // data passed to Excel template
            context.putVar("items", getResellerOfflineReport(resellerId));

            JxlsHelper.getInstance()
                    .setUseFastFormulaProcessor(false)
                    .processTemplate(inputStream, outputStream, context);
        }
    }

}

