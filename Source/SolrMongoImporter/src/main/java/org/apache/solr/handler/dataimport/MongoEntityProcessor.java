package org.apache.solr.handler.dataimport;

import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: James
 * Date: 14/08/12
 * Time: 09:26
 */


public class MongoEntityProcessor extends EntityProcessorBase {
    private static final Logger LOG = LoggerFactory.getLogger(MongoEntityProcessor.class);

    protected MongoDataSource dataSource;

    private String collection;

    @Override
    public void init(Context context) {
        LOG.info( "ENTERED INIT METHOD" );
        super.init(context);
        this.collection = context.getEntityAttribute( COLLECTION );
        LOG.info( "collection : " + context.getEntityAttribute( COLLECTION ) );
        if( this.collection == null ) {
            throw new DataImportHandlerException(SEVERE, "Collection must be supplied");
        }
        LOG.info( "DataSource Name : " + context.getEntityAttribute( "dataSource" ) );
        //new MongoDataSource().init( context, null );
        LOG.info( "Data Source : " + context.getDataSource(context.getEntityAttribute( "dataSource" )) );
        this.dataSource  = (MongoDataSource) context.getDataSource(context.getEntityAttribute( "dataSource" ));
        LOG.info( "Data Source : " + this.dataSource );
        LOG.info( "Finished the Init Method" );
    }

    protected void initQuery(String q) {
        try {
            DataImporter.QUERY_COUNT.get().incrementAndGet();
            rowIterator = dataSource.getData( q, this.collection );
            this.query = q;
        } catch (DataImportHandlerException e) {
            throw e;
        } catch (Exception e) {
            LOG.error( "The query failed '" + q + "'", e);
            throw new DataImportHandlerException(DataImportHandlerException.SEVERE, e);
        }
    }

    @Override
    public Map<String, Object> nextRow() {
        String query = context.getEntityAttribute( QUERY );
        if (rowIterator == null) {
            initQuery(context.replaceTokens(query));
        }
        return getNext();
    }

    public static final String QUERY      = "query";
    public static final String COLLECTION = "collection";
}
