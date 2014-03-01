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

package org.pentaho.di.ui.trans.steps.memcached;

import java.net.InetSocketAddress;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.memcached.MemcachedInputMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class MemcachedInputDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = MemcachedInputMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private MemcachedInputMeta input;
  private boolean gotPreviousFields = false;
  private RowMetaInterface previousFields;

  private Label wlKeyField;
  private CCombo wKeyField;
  private FormData fdlKeyField, fdKeyField;

  private Label wlValueField;
  private TextVar wValueField;
  private FormData fdlValueField, fdValueField;

  private Label wlTypeField;
  private CCombo wTypeField;
  private FormData fdlTypeField, fdTypeField;

  private Composite wServersComp;
  private Label wlServers;
  private TableView wServers;
  private FormData fdlServers, fdServers;

  public MemcachedInputDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    input = (MemcachedInputMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "MemcachedInputDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "MemcachedInputDialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    // Key field
    wlKeyField = new Label( shell, SWT.RIGHT );
    wlKeyField.setText( BaseMessages.getString( PKG, "MemcachedInputDialog.KeyField.Label" ) );
    props.setLook( wlKeyField );
    fdlKeyField = new FormData();
    fdlKeyField.left = new FormAttachment( 0, 0 );
    fdlKeyField.right = new FormAttachment( middle, -margin );
    fdlKeyField.top = new FormAttachment( wStepname, margin );
    wlKeyField.setLayoutData( fdlKeyField );
    wKeyField = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wKeyField );
    wKeyField.addModifyListener( lsMod );
    fdKeyField = new FormData();
    fdKeyField.left = new FormAttachment( middle, 0 );
    fdKeyField.top = new FormAttachment( wStepname, margin );
    fdKeyField.right = new FormAttachment( 100, 0 );
    wKeyField.setLayoutData( fdKeyField );
    wKeyField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getFieldsInto( wKeyField );
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Value field
    wlValueField = new Label( shell, SWT.RIGHT );
    wlValueField.setText( BaseMessages.getString( PKG, "MemcachedInputDialog.ValueField.Label" ) );
    props.setLook( wlValueField );
    fdlValueField = new FormData();
    fdlValueField.left = new FormAttachment( 0, 0 );
    fdlValueField.right = new FormAttachment( middle, -margin );
    fdlValueField.top = new FormAttachment( wKeyField, margin );
    wlValueField.setLayoutData( fdlValueField );
    wValueField = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wValueField );
    wValueField.addModifyListener( lsMod );
    fdValueField = new FormData();
    fdValueField.left = new FormAttachment( middle, 0 );
    fdValueField.top = new FormAttachment( wKeyField, margin );
    fdValueField.right = new FormAttachment( 100, 0 );
    wValueField.setLayoutData( fdValueField );

    // Type field
    wlTypeField = new Label( shell, SWT.RIGHT );
    wlTypeField.setText( BaseMessages.getString( PKG, "MemcachedInputDialog.TypeField.Label" ) );
    props.setLook( wlTypeField );
    fdlTypeField = new FormData();
    fdlTypeField.left = new FormAttachment( 0, 0 );
    fdlTypeField.right = new FormAttachment( middle, -margin );
    fdlTypeField.top = new FormAttachment( wValueField, margin );
    wlTypeField.setLayoutData( fdlTypeField );
    wTypeField = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wTypeField );
    wTypeField.addModifyListener( lsMod );
    fdTypeField = new FormData();
    fdTypeField.left = new FormAttachment( middle, 0 );
    fdTypeField.top = new FormAttachment( wValueField, margin );
    fdTypeField.right = new FormAttachment( 100, 0 );
    wTypeField.setLayoutData( fdTypeField );
    wTypeField.setItems( ValueMeta.getAllTypes() );

    ColumnInfo[] colinf =
        new ColumnInfo[] {
          new ColumnInfo( BaseMessages.getString( PKG, "MemcachedInputDialog.HostName.Column" ),
              ColumnInfo.COLUMN_TYPE_TEXT, false ),
          new ColumnInfo( BaseMessages.getString( PKG, "MemcachedInputDialog.Port.Column" ),
              ColumnInfo.COLUMN_TYPE_TEXT, false ), };

    // Servers
    wServersComp = new Composite( wTypeField, SWT.NONE );
    props.setLook( wServersComp );

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wServersComp.setLayout( fileLayout );

    wlServers = new Label( shell, SWT.RIGHT );
    wlServers.setText( BaseMessages.getString( PKG, "MemcachedInputDialog.Servers.Label" ) );
    props.setLook( wlServers );
    fdlServers = new FormData();
    fdlServers.left = new FormAttachment( 0, 0 );
    fdlServers.right = new FormAttachment( middle, -margin );
    fdlServers.top = new FormAttachment( wTypeField, margin );
    wlServers.setLayoutData( fdlServers );

    wServers =
        new TableView( transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 5/* FieldsRows */, lsMod,
            props );

    fdServers = new FormData();
    fdServers.left = new FormAttachment( middle, 0 );
    fdServers.top = new FormAttachment( wTypeField, margin );
    fdServers.right = new FormAttachment( 100, 0 );
    // fdServers.bottom = new FormAttachment( 100, 0 );
    wServers.setLayoutData( fdServers );

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, wServers );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() )
        display.sleep();
    }
    return stepname;
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( !Const.isEmpty( input.getKeyFieldName() ) ) {
      wKeyField.setText( input.getKeyFieldName() );
    }
    if ( !Const.isEmpty( input.getValueFieldName() ) ) {
      wValueField.setText( input.getValueFieldName() );
    }
    if ( !Const.isEmpty( input.getValueTypeName() ) ) {
      wTypeField.setText( input.getValueTypeName() );
    }

    int i = 0;
    for ( InetSocketAddress addr : input.getServers() ) {

      TableItem item = wServers.table.getItem( i );
      int col = 1;

      item.setText( col++, addr.getHostName() );
      item.setText( col++, Integer.toString( addr.getPort() ) );

    }

    wServers.setRowNums();
    wServers.optWidth( true );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void ok() {
    if ( Const.isEmpty( wStepname.getText() ) )
      return;

    stepname = wStepname.getText(); // return value
    input.setKeyFieldName( wKeyField.getText() );
    input.setValueFieldName( wValueField.getText() );
    input.setValueTypeName( wTypeField.getText() );

    int nrServers = wServers.nrNonEmpty();

    input.allocate( nrServers );

    Set<InetSocketAddress> servers = input.getServers();

    for ( int i = 0; i < nrServers; i++ ) {
      TableItem item = wServers.getNonEmpty( i );
      servers.add( new InetSocketAddress( item.getText( 1 ), Integer.parseInt( item.getText( 2 ) ) ) );
    }
    input.setServers( servers );

    dispose();
  }

  private void getFieldsInto( CCombo fieldCombo ) {
    try {
      if ( !gotPreviousFields ) {
        previousFields = transMeta.getPrevStepFields( stepname );
      }

      String field = fieldCombo.getText();

      if ( previousFields != null ) {
        fieldCombo.setItems( previousFields.getFieldNames() );
      }

      if ( field != null )
        fieldCombo.setText( field );
      gotPreviousFields = true;

    } catch ( KettleException ke ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "MemcachedInputDialog.FailedToGetFields.DialogTitle" ),
          BaseMessages.getString( PKG, "MemcachedInputDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }
}
