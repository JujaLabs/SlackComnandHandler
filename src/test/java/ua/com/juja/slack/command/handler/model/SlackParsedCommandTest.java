package ua.com.juja.slack.command.handler.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ua.com.juja.slack.command.handler.exception.ParseSlackCommandException;

import java.util.*;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;

/**
 * @author Konstantin Sergey
 * @author Nikolay Horushko
 */
public class SlackParsedCommandTest {
    private List<UserData> usersInText;
    private UserData fromUser;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        fromUser = new UserData("uuid0", "@from");
        usersInText = new ArrayList<>();
    }

    @Test
    public void getFirstUserInTextIfOneSlackNameInText() {
        //given
        UserData userInText = new UserData("uuid1", "@slack1");
        usersInText.add(userInText);
        String text = "text text @slack1 text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        UserData result = slackParsedCommand.getFirstUserFromText();
        //then
        assertEquals(userInText, result);
    }

    @Test
    public void getFirstUserInTextIfSomeSlackNameInText() {
        //given
        UserData userInText1 = new UserData("uuid1", "@slack1");
        UserData userInText2 = new UserData("uuid2", "@slack2");
        UserData userInText3 = new UserData("uuid3", "@slack3");
        usersInText.add(userInText1);
        usersInText.add(userInText2);
        usersInText.add(userInText3);
        String text = "text text @slack1 text @slack2 text @slack3";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        UserData result = slackParsedCommand.getFirstUserFromText();
        //then
        assertEquals(userInText1, result);
    }

    @Test
    public void getFirstUserInTextIfOneSlackNameInTextWithoutSpace() {
        //given
        UserData userInText1 = new UserData("uuid1", "@slack1");
        usersInText.add(userInText1);
        String text = "text text@slack1 text text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        UserData result = slackParsedCommand.getFirstUserFromText();
        //then
        assertEquals(userInText1, result);
    }

    @Test
    public void getFirstUserInTextThrowExceptionIfNotUser() {
        //given
        String text = "text text text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(ParseSlackCommandException.class);
        thrown.expectMessage(containsString("The text 'text text text' doesn't contain any slack names"));
        //when
        slackParsedCommand.getFirstUserFromText();
    }

    @Test
    public void getAllUsers() {
        //given
        UserData userInText1 = new UserData("uuid1", "@slack1");
        UserData userInText2 = new UserData("uuid2", "@slack2");
        UserData userInText3 = new UserData("uuid3", "@slack3");
        usersInText.add(userInText1);
        usersInText.add(userInText2);
        usersInText.add(userInText3);
        String text = "text @slack3 text@slack2 text @slack1";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        List<UserData> result = slackParsedCommand.getAllUsersFromText();
        //then
        assertEquals(usersInText, result);
    }

    @Test
    public void getAllUsersIfNotSlackNameInText() {
        //given
        String text = "text text text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        List<UserData> result = slackParsedCommand.getAllUsersFromText();
        //then
        assertEquals(usersInText, result);
    }

    @Test
    public void getText() {
        //given
        String text = "text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        String result = slackParsedCommand.getText();
        //then
        assertEquals("text", result);
    }

    @Test
    public void getFromUser() {
        //given
        String text = "text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        UserData result = slackParsedCommand.getFromUser();
        //then
        assertEquals(fromUser, result);
    }

    @Test
    public void getUserCountIfOneUser() {
        //given
        usersInText.add(new UserData("uuid1", "@slack1"));
        String text = "text @slack1 text";
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        assertEquals(1, slackParsedCommand.getUserCountInText());
    }

    @Test
    public void getUserCountIfNotUser() {
        //given
        String text = "text text";
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        assertEquals(0, slackParsedCommand.getUserCountInText());
    }

    @Test
    public void getUserCountIfManyUsers() {
        //given
        usersInText.add(new UserData("uuid1", "@slack1"));
        usersInText.add(new UserData("uuid2", "@slack2"));
        usersInText.add(new UserData("uuid3", "@slack3"));
        String text = "text @slack1 text @slack2 text @slack3";
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        assertEquals(3, slackParsedCommand.getUserCountInText());
    }


    @Test
    public void getTextWithoutSlackNames() {
        //given
        String text = "@slack text  @slack text @slack";
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        assertEquals("text text", slackParsedCommand.getTextWithoutSlackNames());
    }

    @Test
    public void getTextWithoutSlackNamesTrimSpaces() {
        //given
        String text = "    @slack text  @slack text @slack    ";
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        assertEquals("text text", slackParsedCommand.getTextWithoutSlackNames());
    }

    @Test
    public void getTextWithoutSlackNamesIfNotSlackNameInText() {
        //given
        String text = "text  text";
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        assertEquals("text text", slackParsedCommand.getTextWithoutSlackNames());
    }

    @Test
    public void getUsersByTokensWithTwoTokensSuccessfullCases() {
        //given
        Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2"}));
        UserData userInText1 = new UserData("uuid1", "@slack1");
        UserData userInText2 = new UserData("uuid2", "@slack2");
        usersInText.add(userInText1);
        usersInText.add(userInText2);

        Map<String, UserData> expectedResult = new LinkedHashMap<>();
        expectedResult.put("-t1", userInText1);
        expectedResult.put("-t2", userInText2);

        ArrayList<String> textList = new ArrayList<>(Arrays.asList(
                "-t1 @slack1 -t2 @slack2",
                "-t2 @slack2 -t1 @slack1",
                "-t1@slack1 -t2@slack2",
                "text -t2 @slack2 text -t1 @slack1 text",
                "text -t2 @slack2 text -t1 @slack1 text",
                "text -t2 @slack2 -t1text @slack1 text"
        ));

        textList.forEach(text -> {
            SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
            //when
            Map<String, UserData> result = slackParsedCommand.getUsersWithTokens(tokens);
            //then
            assertEquals(expectedResult.size(), result.size());
            assertEquals(expectedResult, result);
        });
    }

    @Test
    public void getUsersByTokensWithThreeTokensSuccessfullCases() {
        //given
        Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2", "-t3"}));
        UserData userInText1 = new UserData("uuid1", "@slack1");
        UserData userInText2 = new UserData("uuid2", "@slack2");
        UserData userInText3 = new UserData("uuid3", "@slack3");
        usersInText.add(userInText1);
        usersInText.add(userInText2);
        usersInText.add(userInText3);

        Map<String, UserData> expectedResult = new LinkedHashMap<>();
        expectedResult.put("-t1", userInText1);
        expectedResult.put("-t2", userInText2);
        expectedResult.put("-t3", userInText3);


        ArrayList<String> textList = new ArrayList<>(Arrays.asList(
                "-t1 @slack1 -t2 @slack2 -t3 @slack3",
                "-t1@slack1 -t2 @slack2 -t3 @slack3",
                "-t1@slack1 -t2@slack2 -t3@slack3",
                "-t1 @slack1 -t3 @slack3 -t2 @slack2",
                "-t1@slack1 -t3@slack3 -t2@slack2",
                "-t1@slack1 text -t3@slack3 text -t2@slack2 text",
                "-t1 text @slack1 text -t3 text @slack3 text -t2 text @slack2 text",
                "-t1 text@slack1 text -t3 text@slack3 text -t2 text@slack2 text",
                "text -t2 @slack2 -t1 text @slack1 text -t3 @slack3"
        ));

        textList.forEach(text -> {
            //when
            SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
            Map<String, UserData> result = slackParsedCommand.getUsersWithTokens(tokens);
            //then
            assertEquals(expectedResult.size(), result.size());
            assertEquals(expectedResult, result);
        });
    }

    @Test
    public void getUsersByTokensIfTokensArrayIsEmpty() {
        //given
        Set<String> tokens = new HashSet<>();
        UserData userInText1 = new UserData("uuid1", "@slack1");
        UserData userInText2 = new UserData("uuid2", "@slack2");
        UserData userInText3 = new UserData("uuid3", "@slack3");
        usersInText.add(userInText1);
        usersInText.add(userInText2);
        usersInText.add(userInText3);

        Map<String, UserData> expectedResult = new LinkedHashMap<>();

        String text = "text @slack1 text @slack2 text @slack3";

        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        Map<String, UserData> result = slackParsedCommand.getUsersWithTokens(tokens);
        //then
        assertEquals(expectedResult.size(), result.size());
        assertEquals(expectedResult, result);

    }

    @Test
    public void getUsersByTokensThrowExceptionIfTokenNotFound() {
        //given
        usersInText.add(new UserData("uuid1", "@slack1"));
        usersInText.add(new UserData("uuid2", "@slack2"));

        Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2"}));
        String text = "text-t2@slack2 text@slack1 text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(ParseSlackCommandException.class);
        thrown.expectMessage(containsString("Token '-t1' didn't find in the string 'text-t2@slack2 text@slack1 text'"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }

    @Test
    public void getUsersByTokensErrorIfNotFoundSlackNameForToken() {
        //given
        usersInText.add(new UserData("uuid1", "@slack1"));
        usersInText.add(new UserData("uuid2", "@slack2"));

        Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2"}));
        String text = "text-t2 @slack2 text -t1 text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(ParseSlackCommandException.class);
        thrown.expectMessage(containsString("The text 'text-t2 @slack2 text -t1 text' doesn't " +
                "contain slackName for token '-t1'"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }

    @Test
    public void getUsersByTokensError2() {
        //given
        usersInText.add(new UserData("uuid1", "@slack1"));
        usersInText.add(new UserData("uuid2", "@slack2"));

        Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2"}));
        String text = "text-t2 -t1@slack2 text text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(ParseSlackCommandException.class);
        thrown.expectMessage(containsString("The text 'text-t2 -t1@slack2 text text' doesn't contain " +
                "slackName for token '-t2'"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }

    @Test
    public void getUsersByTokensError3() {
        //given
        usersInText.add(new UserData("uuid1", "@slack1"));
        usersInText.add(new UserData("uuid2", "@slack2"));
        usersInText.add(new UserData("uuid3", "@slack3"));

        Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2", "-t3"}));
        String text = "-t1 @slack1 -t2 -t3 @slack2 -t3 @slack3";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(ParseSlackCommandException.class);
        thrown.expectMessage(containsString("The text '-t1 @slack1 -t2 -t3 @slack2 -t3 @slack3'" +
                " contains 2 tokens '-t3', but expected 1"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }

    @Test
    public void getUsersByTokensErrorTextContainsMoreThanOneToken() {
        //given
        usersInText.add(new UserData("uuid1", "@slack1"));
        usersInText.add(new UserData("uuid2", "@slack2"));

        Set<String> tokens = new HashSet<>(Arrays.asList(new String[]{"-t1", "-t2"}));
        String text = "text-t2 -t1@slack2 text -t1 text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(ParseSlackCommandException.class);
        thrown.expectMessage(containsString("The text 'text-t2 -t1@slack2 text -t1 text' contains 2 tokens '-t1'," +
                " but expected 1"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }
}

