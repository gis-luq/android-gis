package com.youngken.query;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.esri.android.map.MapView;
import com.esri.core.tasks.identify.IdentifyParameters;
import com.esri.core.tasks.identify.IdentifyResult;
import com.esri.core.tasks.identify.IdentifyTask;
import com.youngken.tool.ApplicationContext;
import com.youngken.zy_gis_demo_001.R;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Young Ken.
 */
public class QueryIdentify
{
    /**
     * 这个属性为空不能设置默认属性，将抛出异常。
     */
    private MapView mapView = null;
    /**
     * 设置查询属性，再这个方法中给了默认属性，也可以自己设置默认属性。
     */
    private QueryIdentfyParameters parameters = null;
    /**
     * 查询URL（服务的URL）不能为空。
     */
    private String queryUrl = null;
    /**
     * 可以为空，为空将不显示进度条。也可以自己定义进度条。
     */
    private ProgressDialog dialog = null;
    /**
     * 查询接口。
     */
    private IQueryIdentityResult queryIdentityResult = null;

    /**
     * 构造函数.
     * @param map 传人MapView 不能为空。
     * @param iqr 实现接口的类.
     */
    public QueryIdentify(MapView map, IQueryIdentityResult iqr)
    {
        this.mapView = map;
        this.queryIdentityResult = iqr;
    }

    /**
     * 执行查询方法
     */
    public final void  execute()
    {
        if (null == parameters)
            parameters = new QueryIdentfyParameters(mapView);
        QueryIdentifyTask task = new QueryIdentifyTask(queryUrl);
        task.execute(parameters);
    }

    /**
     * 查询任务，通过回调得到返回值。
     */
    private class QueryIdentifyTask extends AsyncTask<QueryIdentfyParameters, Void, IdentifyResult[]>
    {
       private IdentifyTask task = null;
       private IdentifyResult[] results = null;
       private String queryUrl = null;
        public QueryIdentifyTask(String queryUrl)
        {
            this.queryUrl = queryUrl;
            try
            {
                if(queryUrl == null)
                    throw new RuntimeException(ApplicationContext.getContext().getResources().getString(R.string.query_url_is_null));
                task = new IdentifyTask(queryUrl);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(IdentifyResult[] identifyResults)
        {
            if(dialog != null)
            {
                if (dialog.isShowing())
                    dialog.dismiss();
            }

            ArrayList<IdentifyResult> resultList = new ArrayList<>();
            IdentifyResult tempResult = null;
            try
            {
                if(identifyResults == null)
                    throw new RuntimeException(ApplicationContext.getContext().getResources().getString(R.string.null_except));

            }catch (Exception e)
            {
                e.printStackTrace();
            }

            for (int i = 0; i < identifyResults.length; i++)
            {
                tempResult = identifyResults[i];
                String displayFieldName = tempResult.getDisplayFieldName();
                Map<String, Object> attr = tempResult.getAttributes();
                for (String key : attr.keySet())
                {
                    if (key.equalsIgnoreCase(displayFieldName))
                    {
                        resultList.add(tempResult);
                    }
                }
            }
            queryIdentityResult.setRestut(resultList);
        }

        @Override
        protected IdentifyResult[] doInBackground(QueryIdentfyParameters... params)
        {
            if(params != null && params.length >= 0)
            {
                IdentifyParameters parameters = params[0];
                try {
                    results = task.execute(parameters);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return results;
        }
        @Override
        protected void onPreExecute()
        {
            if(dialog == null && mapView != null)
            dialog =  ProgressDialog.show(mapView.getContext(), "查询",
                    "正在进行查询 ...");
        }
    }

    public String getQueryUrl()
    {
        return queryUrl;
    }
    public void setQueryUrl(String queryUrl)
    {
        this.queryUrl = queryUrl;
    }

    public QueryIdentfyParameters getParameters()
    {
        return parameters;
    }

    public void setParameters(QueryIdentfyParameters parameters)
    {
        this.parameters = parameters;
    }

    public ProgressDialog getDialog()
    {
        return dialog;
    }
    public void setDialog(ProgressDialog dialog)
    {
        this.dialog = dialog;
    }
}
