/*******************************************************************************
 * Copyright (c) 2020 Konduit K.K.
 * Copyright (c) 2015-2019 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package com.bolingcavalry.commons.utils;

import org.apache.commons.io.FilenameUtils;
import org.nd4j.common.resources.Downloader;

import java.io.File;
import java.net.URL;

/**
 * Given a base url and a zipped file name downloads contents to a specified directory under ~/dl4j-examples-data
 * Will check md5 sum of downloaded file
 * <p>
 *
 * Sample Usage with an instantiation DATAEXAMPLE(baseurl,"DataExamples.zip","data-dir",md5,size):
 *
 * DATAEXAMPLE.Download() & DATAEXAMPLE.Download(true)
 * Will download DataExamples.zip from baseurl/DataExamples.zip to a temp directory,
 * Unzip it to ~/dl4j-example-data/data-dir
 * Return the string "~/dl4j-example-data/data-dir/DataExamples"
 *
 * DATAEXAMPLE.Download(false)
 * will perform the same download and unzip as above
 * But returns the string "~/dl4j-example-data/data-dir" instead
 *
 *
 * @author susaneraly
 */
public enum DownloaderUtility {

    IRISDATA("IrisData.zip", "datavec-examples", "bb49e38bb91089634d7ef37ad8e430b8", "1KB"),
    ANIMALS("animals.zip", "dl4j-examples", "1976a1f2b61191d2906e4f615246d63e", "820KB"),
    ANOMALYSEQUENCEDATA("anomalysequencedata.zip", "dl4j-examples", "51bb7c50e265edec3a241a2d7cce0e73", "3MB"),
    CAPTCHAIMAGE("captchaImage.zip", "dl4j-examples", "1d159c9587fdbb1cbfd66f0d62380e61", "42MB"),
    CLASSIFICATIONDATA("classification.zip", "dl4j-examples", "dba31e5838fe15993579edbf1c60c355", "77KB"),
    DATAEXAMPLES("DataExamples.zip", "dl4j-examples", "e4de9c6f19aaae21fed45bfe2a730cbb", "2MB"),
    LOTTERYDATA("lottery.zip", "dl4j-examples", "1e54ac1210e39c948aa55417efee193a", "2MB"),
    NEWSDATA("NewsData.zip", "dl4j-examples", "0d08e902faabe6b8bfe5ecdd78af9f64", "21MB"),
    NLPDATA("nlp.zip", "dl4j-examples", "1ac7cd7ca08f13402f0e3b83e20c0512", "91MB"),
    PREDICTGENDERDATA("PredictGender.zip", "dl4j-examples", "42a3fec42afa798217e0b8687667257e", "3MB"),
    STYLETRANSFER("styletransfer.zip", "dl4j-examples", "b2b90834d667679d7ee3dfb1f40abe94", "3MB"),
    VIDEOEXAMPLE("video.zip","dl4j-examples", "56274eb6329a848dce3e20631abc6752", "8.5MB");

    private final String BASE_URL;
    private final String DATA_FOLDER;
    private final String ZIP_FILE;
    private final String MD5;
    private final String DATA_SIZE;
    private static final String AZURE_BLOB_URL = "https://dl4jdata.blob.core.windows.net/dl4j-examples";

    /**
     * For use with resources uploaded to Azure blob storage.
     *
     * @param zipFile    Name of zipfile. Should be a zip of a single directory with the same name
     * @param dataFolder The folder to extract to under ~/dl4j-examples-data
     * @param md5        of zipfile
     * @param dataSize   of zipfile
     */
    DownloaderUtility(String zipFile, String dataFolder, String md5, String dataSize) {
        this(AZURE_BLOB_URL + "/" + dataFolder, zipFile, dataFolder, md5, dataSize);
    }

    /**
     * Downloads a zip file from a base url to a specified directory under the user's home directory
     *
     * @param baseURL    URL of file
     * @param zipFile    Name of zipfile to download from baseURL i.e baseURL+"/"+zipFile gives full URL
     * @param dataFolder The folder to extract to under ~/dl4j-examples-data
     * @param md5        of zipfile
     * @param dataSize   of zipfile
     */
    DownloaderUtility(String baseURL, String zipFile, String dataFolder, String md5, String dataSize) {
        BASE_URL = baseURL;
        DATA_FOLDER = dataFolder;
        ZIP_FILE = zipFile;
        MD5 = md5;
        DATA_SIZE = dataSize;
    }

    public String Download() throws Exception {
        return Download(true);
    }

    public String Download(boolean returnSubFolder) throws Exception {
        String dataURL = BASE_URL + "/" + ZIP_FILE;
        String downloadPath = FilenameUtils.concat(System.getProperty("java.io.tmpdir"), ZIP_FILE);
        String extractDir = FilenameUtils.concat(System.getProperty("user.home"), "dl4j-examples-data/" + DATA_FOLDER);
        if (!new File(extractDir).exists())
            new File(extractDir).mkdirs();
        String dataPathLocal = extractDir;
        if (returnSubFolder) {
            String resourceName = ZIP_FILE.substring(0, ZIP_FILE.lastIndexOf(".zip"));
            dataPathLocal = FilenameUtils.concat(extractDir, resourceName);
        }
        int downloadRetries = 10;
        if (!new File(dataPathLocal).exists() || new File(dataPathLocal).list().length == 0) {
            System.out.println("_______________________________________________________________________");
            System.out.println("Downloading data (" + DATA_SIZE + ") and extracting to \n\t" + dataPathLocal);
            System.out.println("_______________________________________________________________________");
            Downloader.downloadAndExtract("files",
                new URL(dataURL),
                new File(downloadPath),
                new File(extractDir),
                MD5,
                downloadRetries);
        } else {
            System.out.println("_______________________________________________________________________");
            System.out.println("Example data present in \n\t" + dataPathLocal);
            System.out.println("_______________________________________________________________________");
        }
        return dataPathLocal;
    }
}
