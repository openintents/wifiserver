package org.openintents.wifiserver.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openintents.wifiserver.util.StringUtil;

public class TestNotesInterface extends BasicServerTest {

    private static final String TAG = TestNotesInterface.class.getSimpleName();
    private Random rand = new Random();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllNotes();
    }
    
    private HttpResponse deleteAllNotes() {
        return doGet("http://127.0.0.1:8080/notes/delete");
    }

    private HttpResponse deleteNote(int id) {
        return doGet("http://127.0.0.1:8080/notes/delete?id=" + id);
    }

    private HttpResponse getAllNotes() {
        return doGet("http://127.0.0.1:8080/notes/get");
    }

    private HttpResponse getNote(int id) {
        return doGet("http://127.0.0.1:8080/notes/get?id=" + id);
    }

    private HttpResponse createNote(Map<String, String> parameters) {
        return doPost("http://127.0.0.1:8080/notes/new", parameters);
    }

    private HttpResponse updateNote(Map<String, String> parameters) {
        return doPost("http://127.0.0.1:8080/notes/update", parameters);
    }

    private Map<String, String> buildTestNote(final boolean id,
            final boolean title, final boolean note, final boolean tags) {
        return new HashMap<String, String>(4) {
            private static final long serialVersionUID = 216769214743685102L;

            {
                if (id)    put("_id", rand.nextInt(1000)+"");
                if (title) put("title", "TestTitle_"+rand.nextInt(1000));
                if (note)  put("note", "TestNote_"+rand.nextInt(1000));
                if (tags)  put("tags", "TestTag_"+rand.nextInt(1000)+", TestTag_"+rand.nextInt(1000));
            }
        };
    }

    /**
     * <pre>
     * Test case:
     *  all notes will be deleted
     *
     * Result:
     *  status code = 200
     *  content is empty
     * </pre>
     */
    public void testDeleteNote() {
        HttpResponse response = deleteAllNotes();

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
    }

    /**
     * <pre>
     * Test case:
     *  Get a list of all notes.
     *  There are no notes in database.
     *
     * Result:
     *  status code = 200
     *  content = "[]"
     * </pre>
     *
     * @throws IOException
     * @throws IllegalStateException
     */
    public void testGetAllNotes_empty() throws IllegalStateException,
            IOException {
        HttpResponse response = getAllNotes();

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(2, response.getEntity().getContentLength());
        assertEquals("[]", StringUtil.fromInputStream(response.getEntity().getContent()));
    }

    /**
     * <pre>
     * Test case:
     *  Get a list of all notes.
     *  There are two notes in database.
     *
     * Result:
     *  status code = 200
     *  content = "[{&lt;note_1&gt;}, {&lt;note_2&gt;}]"
     * </pre>
     */
    public void testGetAllNotes_notEmpty() {
        Map<String, String> note1 = buildTestNote(false, true, true, false);
        Map<String, String> note2 = buildTestNote(false, true, true, false);

        createNote(note1);
        createNote(note2);

        HttpResponse response = getAllNotes();

        try {
            String stringArray = StringUtil.fromInputStream(response
                    .getEntity().getContent());
            JSONArray jsonArray = new JSONArray(stringArray);

            assertEquals(2, jsonArray.length());

            JSONObject actualNote1 = jsonArray.getJSONObject(0);
            JSONObject actualNote2 = jsonArray.getJSONObject(1);

            assertTrue(
                (
                    actualNote1.getString("title").equals(note1.get("title"))
                    &&
                    actualNote1.getString("note").equals(note1.get("note"))
                    &&
                    actualNote2.getString("title").equals(note2.get("title"))
                    &&
                    actualNote2.getString("note").equals(note2.get("note"))
                )
                ||
                (
                    actualNote1.getString("title").equals(note2.get("title"))
                    &&
                    actualNote1.getString("note").equals(note2.get("note"))
                    &&
                    actualNote2.getString("title").equals(note1.get("title"))
                    &&
                    actualNote2.getString("note").equals(note1.get("note"))
                )
            );

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assertEquals(200, response.getStatusLine().getStatusCode());

    }

    /**
     * <pre>
     * Test case:
     *  create note
     *
     * Result:
     *  status code = 200
     *  content is empty
     * </pre>
     */
    public void testCreateNote() {
        HttpResponse response = createNote(buildTestNote(false, true, true,
                false));

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
    }

    /**
     * <pre>
     * Test case:
     *  request a particular note by id
     *  There are no notes.
     *
     * Result:
     *  status code = 404
     *  content is empty
     * </pre>
     */
    public void testDeleteAndGetParticularNote() {
        HttpResponse response = getNote(rand.nextInt(1000));

        assertEquals(404, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
    }

    /**
     * <pre>
     * Test case:
     *  create new note without "note"-parameter
     *
     * Result:
     *  status code = 400
     *  content is empty
     * </pre>
     */
    public void testCreateMissingNoteParam() {
        HttpResponse response = createNote(buildTestNote(false, true, false,
                false));

        assertEquals(400, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
    }

    /**
     * <pre>
     * Test case:
     *  create new note without "title"-parameter
     *
     * Result:
     *  status code = 400
     *  content is empty
     * </pre>
     */
    public void testCreateMissingTitleParam() {
        HttpResponse response = createNote(buildTestNote(false, false, true,
                false));

        assertEquals(400, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
    }

    /**
     * <pre>
     * Test case:
     *  update non existing note
     *
     * Result:
     *  status code = 400
     *  content is empty
     * </pre>
     */
    public void testUpdateNote_nonExistend() {
        HttpResponse response = updateNote(buildTestNote(true, true, true,
                false));

        assertEquals(400, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
    }

    /**
     * <pre>
     * Test case:
     *  update existing note
     *
     * Result:
     *  status code = 200
     *  content is empty
     * </pre>
     */
    public void testUpdateNote_Existend() {
        Map<String, String> note = buildTestNote(false, true, true, false);
        createNote(note);

        String id=null;

        try {
            String stringArray = StringUtil.fromInputStream(getAllNotes()
                    .getEntity().getContent());
            JSONArray jsonArray = new JSONArray(stringArray);
            JSONObject actualNote = jsonArray.getJSONObject(0);

            id = actualNote.get("_id").toString();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        note.put("_id", id);
        note.put("note", "updated test note");

        HttpResponse response = updateNote(note);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
    }
}