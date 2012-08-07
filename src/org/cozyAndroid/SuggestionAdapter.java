package org.cozyAndroid;
import org.codehaus.jackson.JsonNode;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult.Row;
import org.ektorp.android.util.CouchbaseViewListAdapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;


public class SuggestionAdapter extends CouchbaseViewListAdapter  implements Filterable {

	private LayoutInflater inflater;
	private String constraint;
	private Filter mFilter = null;
	private static int black;
	
	public SuggestionAdapter(CouchDbConnector couchDbConnector, ViewQuery viewQuery, Context context) {
		super(couchDbConnector, viewQuery, true);
		inflater = LayoutInflater.from(context);
		black = context.getResources().getColor(android.R.color.black);
	}

	private static class ViewHolder {
	   TextView title;
	   ImageView button;
	}

	@Override
	public View getView(int position, View itemView, ViewGroup parent) {
        View v = itemView;
        ViewHolder vh;
        if (v == null) {
            v = inflater.inflate(R.layout.suggestion, null);
            vh = new ViewHolder();
            vh.title = (TextView) v.findViewById(R.id.textSuggestion);
            vh.button = (ImageView) v.findViewById(R.id.buttonSuggestion);
            v.setTag(vh);
        } else {
        	vh = (ViewHolder) v.getTag();
        }
        Row row = getRow(position);
        JsonNode item = row.getValueAsNode();
        JsonNode itemText = item.get("title");
        if(itemText != null) {
        	CharSequence text = itemText.getTextValue();
			Spannable textSpan = new SpannableString(text);
        	int start = text.toString().toLowerCase().indexOf(constraint);
			if (start != -1) {
				ForegroundColorSpan blacktext = new ForegroundColorSpan(black);
				textSpan.setSpan(blacktext, start, start + constraint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
        	vh.title.setText(textSpan);
        }
        else {
        	vh.title.setText("");
        }
        return v;
	}
	
	public Filter getFilter () {
		if (mFilter == null) {
			mFilter = new SFilter();
		}
		return mFilter;
	}
	
	private class SFilter extends Filter {

		@Override
		public CharSequence convertResultToString (Object resultValue) {
			Row row = (Row) resultValue;
			JsonNode item = row.getValueAsNode();
			return item.get("title").getTextValue();
		}
		
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults res = new FilterResults();
			// TODO Auto-generated method stub
			if (constraint == null || constraint.equals("")) {
				res.count = 0;
				return res;
			}
			SuggestionAdapter.this.constraint = constraint.toString().toLowerCase();
			String start = constraint.toString().toLowerCase();
			String end = start.toUpperCase() + "\u9999";
			SuggestionAdapter.this.viewQuery.startKey(start).endKey(end);
			SuggestionAdapter.this.updateListItems();
			res.count = getCount();
			Log.d("filtrage suggestions","count = " + res.count);
			res.values = null;
			return res;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
		}
		
	}
}
