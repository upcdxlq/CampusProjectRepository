package gov.nasa.worldwind.layer;

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
 * Created by Lenovo on 2018/4/3.
 */
public class TiandituCia_cLayer extends RenderableLayer implements TileFactory {

    //protected TileFactory tiandituTileFactory;

    String urlAddress = "";

    public TiandituCia_cLayer() {
        this("http://t0.tianditu.com/cia_c/wmts");
    }

    public TiandituCia_cLayer(String serviceAddress) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BlueMarbleLandsatLayer", "constructor", "missingServiceAddress"));
        }

        urlAddress = serviceAddress;
        //tiandituTileFactory = new WmtsTileFactory();

        // Configure this layer's level set to capture the entire globe at 15m resolution.
        double metersPerPixel = 15;
        double radiansPerPixel = metersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS;
        LevelSetConfig levelsConfig = new LevelSetConfig(null, 45, 16, 256, 256);
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
        ImageTile tile = new ImageTile(sector, level, row, column);

        //String urlString = urlAddress;//this.urlForTile(level.levelNumber, row, column);

        int row1 = (int) Math.pow(2, (level.levelNumber + 2)) - 1 - row;//计算行列和级数
        int col1 = column;
        int level1 = level.levelNumber + 3;

        String serverURL = urlAddress.replaceFirst("0", String.valueOf((int) (Math.random() * 8)));//由于服务器端采用了集群技术，http://tile0/同http://tile7/取的是同一图片
        //瓦片URL串
        String urlString = serverURL + "?request=GetTile&service=wmts&version=1.0.0&serviceMode=kvp&layer=cia&Style=default&Format=tiles&TileMatrixSet=c&TileMatrix=" + level1 + "&TileRow=" + row1 + "&TileCol=" + col1;

        if (urlString != null) {
            tile.setImageSource(ImageSource.fromUrl(urlString));
        }

        return tile;
    }
}