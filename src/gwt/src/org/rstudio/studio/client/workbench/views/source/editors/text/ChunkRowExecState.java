/*
 * ChunkRowExecState.java
 *
 * Copyright (C) 2009-16 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.workbench.views.source.editors.text;

import org.rstudio.studio.client.workbench.views.source.editors.text.ace.AceEditorNative;
import org.rstudio.studio.client.workbench.views.source.editors.text.ace.Anchor;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;

public class ChunkRowExecState
{
   public ChunkRowExecState(final AceEditorNative editor, int row, int state,
         Command onRemoved)
   {
      anchor_ = Anchor.createAnchor(editor.getSession().getDocument(), row, 0);
      editor_ = editor;
      row_ = row;
      state_ = state;
      onRemoved_ = onRemoved;
      anchor_.addOnChangeHandler(new Command()
      {
         @Override
         public void execute()
         {
            if (getRow() == anchor_.getRow())
               return;
            editor.getRenderer().removeGutterDecoration(
               getRow() - 1, getClazz());
            row_ = anchor_.getRow();
            addClazz();
         }
      });

      editor.getRenderer().addGutterDecoration(getRow() - 1, getClazz());
   }
   
   public int getRow()
   {
      return row_;
   }
   public void setRow(int row)
   {
      row_ = row;
   }

   public void detach()
   {
      resetTimer();
      editor_.getRenderer().removeGutterDecoration(getRow() - 1, 
            LINE_QUEUED_CLASS);
      editor_.getRenderer().removeGutterDecoration(getRow() - 1, 
            LINE_EXECUTED_CLASS);
      editor_.getRenderer().removeGutterDecoration(getRow() - 1, 
            LINE_RESTING_CLASS);
      anchor_.detach();
      if (onRemoved_ != null)
         onRemoved_.execute();
   }
   
   public int getState()
   {
      return state_;
   }
   
   public String getClazz()
   {
      switch (state_)
      {
      case LINE_QUEUED:
         return LINE_QUEUED_CLASS;
      case LINE_EXECUTED:
         return LINE_EXECUTED_CLASS;
      case LINE_RESTING:
         return LINE_RESTING_CLASS;
      }
      return "";
   }
   
   public void setState(int state)
   {
      state_ = state;
      if (state_ ==  LINE_RESTING)
      {
         timer_ = new Timer()
         {
            @Override
            public void run()
            {
               addClazz();
               scheduleDismiss();
            }
         };
         timer_.schedule(LINGER_MS);
      }
      else
      {
         addClazz();
      }
   }
   
   private void addClazz()
   {
      editor_.getRenderer().addGutterDecoration(getRow() - 1, getClazz());
   }
   
   private void scheduleDismiss()
   {
      resetTimer();
      timer_ = new Timer()
      {
         @Override
         public void run()
         {
            detach();
         }
      };
      timer_.schedule(FADE_MS);
   }
   
   private void resetTimer()
   {
      if (timer_ != null && timer_.isRunning())
      {
         timer_.cancel();
         timer_ = null;
      }
   }

   private int row_;
   private int state_;

   private final Anchor anchor_;
   private final AceEditorNative editor_;
   private final Command onRemoved_;
   
   private Timer timer_;
   
   private final static int LINGER_MS = 250;
   private final static int FADE_MS   = 400;

   public final static String LINE_QUEUED_CLASS = "ace_chunk-queued-line";
   public final static String LINE_EXECUTED_CLASS = "ace_chunk-executed-line";
   public final static String LINE_RESTING_CLASS = "ace_chunk-resting-line";
   
   public final static int LINE_RESTING  = 0;
   public final static int LINE_QUEUED   = 1;
   public final static int LINE_EXECUTED = 2;
}
