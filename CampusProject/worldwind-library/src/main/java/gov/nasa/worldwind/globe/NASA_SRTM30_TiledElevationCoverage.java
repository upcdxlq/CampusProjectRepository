package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.Wcs100ElevationCoverage;

/**
 * Created by Lenovo on 2018/4/4.
 */

public class NASA_SRTM30_TiledElevationCoverage extends Wcs100ElevationCoverage{
    // Specify the bounding sector - provided by the WCS
    Sector coverageSector = Sector.fromDegrees(-90, -180.0, 180, 360.0);
    // Specify the number of levels to match data resolution
    int numberOfLevels = 12;
    // Specify the version 1.0.0 WCS address
    String serviceAddress = "https://worldwind26.arc.nasa.gov/wcs";
    // Specify the coverage name
    String coverage = "NASA_SRTM30_900m_Tiled";

    public  NASA_SRTM30_TiledElevationCoverage() {
        // Create an elevation coverage from a version 1.0.0 WCS
      super(Sector.fromDegrees(-90, -180.0, 180, 360.0),
              12, "https://worldwind26.arc.nasa.gov/wcs",  "NASA_SRTM30_900m_Tiled");
    }

}
