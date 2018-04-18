package gov.nasa.worldwind.layer;

/**
 * Created by Lenovo on 2018/4/2.
 */

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.ImageOptions;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.ImageTile;
import gov.nasa.worldwind.shape.TiledSurfaceImage;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.LevelSetConfig;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

/**
 * Created by Lenovo on 2018/4/1.
 */

public class TiandituCva_wLayer extends RenderableLayer implements TileFactory {

    //protected TileFactory tiandituTileFactory;

    String urlAddress = "";

    public TiandituCva_wLayer() {
        this("http://t0.tianditu.com/DataServer");
    }

    public TiandituCva_wLayer(String serviceAddress) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BlueMarbleLandsatLayer", "constructor", "missingServiceAddress"));
        }

        urlAddress = serviceAddress;
        //tiandituTileFactory = new WmtsTileFactory();

        // Configure this layer's level set to capture the entire globe at 15m resolution.
        double metersPerPixel = 15;
        double radiansPerPixel = metersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS;
        LevelSetConfig levelsConfig = new LevelSetConfig(null, 45, 16, 512, 256);
        //levelsConfig.numLevels = levelsConfig.numLevelsForResolution(radiansPerPixel);

        this.setDisplayName("TiandituSat");
        this.setPickEnabled(false);

        TiledSurfaceImage surfaceImage = new TiledSurfaceImage();
        surfaceImage.setLevelSet(new LevelSet(levelsConfig));
        surfaceImage.setTileFactory(this);
       // surfaceImage.setImageOptions(new ImageOptions(WorldWind.RGB_565)); // reduce memory usage by using a 16-bit configuration with no alpha
        this.addRenderable(surfaceImage);

    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {

        Sector sector1 = new Sector(sector.minLatitude(), sector.minLongitude() * 2 + 180, sector.deltaLatitude(), sector.deltaLongitude() * 2);
        ImageTile tile = new ImageTile(sector1, level, row, column);
        if(level.levelNumber>0){
            sector1 = new Sector(sector.minLatitude(), sector.minLongitude() , sector.deltaLatitude(), sector.deltaLongitude() * 2);
            tile = new ImageTile(sector1, level, row, column);
        }
        //urlAddress= "http://t0.tianditu.com/DataServer";
        //String urlString = urlAddress;//this.urlForTile(level.levelNumber, row, column);
        int row1 = (int) Math.pow(2, (level.levelNumber + 2)) - 1 - row;//计算行列和jishu
        int col1 = column;

        int level1 = level.levelNumber + 2;

        String serverURL = urlAddress.replaceFirst("0", String.valueOf((int) (Math.random() * 8)));//由于服务器端采用了集群技术，http://tile0/同http://tile7/取的是同一图片
        //瓦片URL串
        String urlString = serverURL + "?T=cva_w&x="+col1+"&y="+row1+"&l="+level1;

        if (urlString != null) {
            tile.setImageSource(ImageSource.fromUrl(urlString));
        }

        return tile;
    }
}