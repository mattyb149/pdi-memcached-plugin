/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.memcached;

import java.net.InetSocketAddress;
import java.util.Set;

import net.spy.memcached.MemcachedClient;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * The Memcached Output step stores value objects, for the given key names, to memached server(s).
 * 
 */
public class MemcachedOutput extends BaseStep implements StepInterface {
  private static Class<?> PKG = MemcachedOutputMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  protected MemcachedOutputMeta meta;
  protected MemcachedOutputData data;

  protected MemcachedClient memcachedClient = null;

  public MemcachedOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( super.init( smi, sdi ) ) {
      try {
        // Create client and connect to memcached server(s)
        Set<InetSocketAddress> servers = ( (MemcachedOutputMeta) smi ).getServers();
        memcachedClient = new MemcachedClient( servers.toArray( new InetSocketAddress[servers.size()] ) );

        return true;
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "MemcachedOutput.Error.ConnectError" ), e );
        return false;
      }
    } else {
      return false;
    }
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (MemcachedOutputMeta) smi;
    data = (MemcachedOutputData) sdi;

    Object[] r = getRow(); // get row, set busy!

    // If no more input to be expected, stop
    if ( r == null ) {
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      // clone input row meta for now, we will change it (add or set inline) later
      data.outputRowMeta = getInputRowMeta().clone();
      // Get output field types
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

    }

    Object[] outputRowData = r;
    
    // Get value from memcached, don't cast now, be lazy. TODO change this?
    int keyFieldIndex = getInputRowMeta().indexOfValue( meta.getKeyFieldName() );
    if ( keyFieldIndex < 0 ) {
      throw new KettleException( BaseMessages.getString( PKG, "MemcachedOutputMeta.Exception.KeyFieldNameNotFound" ) );
    }
    int valueFieldIndex = getInputRowMeta().indexOfValue( meta.getValueFieldName() );
    if ( valueFieldIndex < 0 ) {
      throw new KettleException( BaseMessages.getString( PKG, "MemcachedOutputMeta.Exception.ValueFieldNameNotFound" ) );
    }
    
    memcachedClient.set( (String) ( r[keyFieldIndex] ), meta.getExpirationTime(), (String) ( r[valueFieldIndex] ) );

    putRow( data.outputRowMeta, outputRowData ); // copy row to possible alternate rowset(s).

    if ( checkFeedback( getLinesRead() ) ) {
      if ( log.isBasic() )
        logBasic( BaseMessages.getString( PKG, "MemcachedOutput.Log.LineNumber" ) + getLinesRead() );
    }

    return true;
  }
}
