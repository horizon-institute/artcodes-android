/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import uk.ac.horizon.artcodes.model.Experience;

public class ExperienceParser {
	private static class ExperienceTypeAdapterFactor implements TypeAdapterFactory {
		@SuppressWarnings("unchecked")
		// we use a runtime check to guarantee that 'C' and 'T' are equal
		public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			return type.getRawType() == Experience.class ? (TypeAdapter<T>) customizeMyClassAdapter(gson, (TypeToken<Experience>) type) : null;
		}

		private TypeAdapter<Experience> customizeMyClassAdapter(Gson gson, TypeToken<Experience> type) {
			final TypeAdapter<Experience> delegate = gson.getDelegateAdapter(this, type);
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

			return new TypeAdapter<Experience>() {
				@Override
				public Experience read(JsonReader in) throws IOException {
					JsonElement tree = elementAdapter.read(in);
					if (tree.isJsonObject()) {
						JsonObject jsonObject = tree.getAsJsonObject();
						if (jsonObject.has("threshold") && jsonObject.get("threshold").isJsonPrimitive()) {
							jsonObject.remove("threshold");
						}
						if (jsonObject.has("id")) {
							String id = jsonObject.get("id").getAsString();
							if (!id.contains(":")) {
								jsonObject.addProperty("id", "http://aestheticodes.appspot.com/experience/" + id);
							}
						}

						if (!jsonObject.has("pipeline")) {
							JsonArray array = new JsonArray();
							array.add(new JsonPrimitive("tile"));
							if (jsonObject.has("embeddedChecksum")
									&& jsonObject.get("embeddedChecksum") instanceof JsonPrimitive
									&& jsonObject.get("embeddedChecksum").getAsBoolean()) {
								array.add(new JsonPrimitive("detectEmbedded"));
							} else {
								array.add(new JsonPrimitive("detect"));
							}

							jsonObject.add("pipeline", array);
						}

						if (jsonObject.has("markers")) {
							JsonElement markers = jsonObject.get("markers");
							jsonObject.add("actions", markers);

							if (markers.isJsonArray()) {
								JsonArray markerArray = markers.getAsJsonArray();
								for (JsonElement element : markerArray) {
									if (element.isJsonObject()) {
										JsonObject actionObject = element.getAsJsonObject();
										if (actionObject.has("title")) {
											actionObject.add("name", actionObject.get("title"));
										}

										if (actionObject.has("action")) {
											actionObject.add("url", actionObject.get("action"));
										}

										if (actionObject.has("code")) {
											JsonArray codeArray = new JsonArray();
											String codeString = actionObject.get("code").getAsString();
											if (codeString.contains("+")) {
												String[] codes = codeString.split("\\+");
												for (String code : codes) {
													codeArray.add(new JsonPrimitive(code));
												}
												actionObject.addProperty("match", "all");
											} else if (codeString.contains(">")) {
												String[] codes = codeString.split(">");
												for (String code : codes) {
													codeArray.add(new JsonPrimitive(code));
												}
												actionObject.addProperty("match", "sequence");
											} else {
												codeArray.add(codeString);
												actionObject.addProperty("match", "any");
											}
											actionObject.add("codes", codeArray);
										}
									}
								}
							}
						}
					}

					return delegate.fromJsonTree(tree);
				}

				@Override
				public void write(JsonWriter out, Experience value) throws IOException {
					delegate.write(out, value);
				}
			};
		}
	}

	private static Gson gson;

	public static Gson createGson(Context context) {
		if (gson == null) {
			GsonBuilder builder = new GsonBuilder();
			if (context == null || ScannerFeatures.load_old_experiences.isEnabled(context)) {
				builder.registerTypeAdapterFactory(new ExperienceTypeAdapterFactor());
			}
			gson = builder.create();
		}
		return gson;
	}
}
