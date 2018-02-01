package com.jci.utils;

import java.io.*;
import java.util.Vector;

public class CSVReader
{

    private static final boolean debugging = true;
    private BufferedReader r;
    private char separator;
    private static final int EOL = 0;
    private static final int ORDINARY = 1;
    private static final int QUOTE = 2;
    private static final int SEPARATOR = 3;
    private static final int WHITESPACE = 4;
    private static final int SEEKINGSTART = 0;
    private static final int INPLAIN = 1;
    private static final int INQUOTED = 2;
    private static final int AFTERENDQUOTE = 3;
    private static final int SKIPPINGTAIL = 4;
    private String line;
    private int lineCount;

    public CSVReader(Reader r, char separator)
    {
        line = null;
        lineCount = 0;
        if(r instanceof BufferedReader)
        {
            this.r = (BufferedReader)r;
        } else
        {
            this.r = new BufferedReader(r);
        }
        this.separator = separator;
        System.out.println("Seperator is : "+this.separator);
        System.out.println("Reader is : "+r.toString());
    }

    public CSVReader(Reader r)
    {
        line = null;
        lineCount = 0;
        if(r instanceof BufferedReader)
        {
            this.r = (BufferedReader)r;
        } else
        {
            this.r = new BufferedReader(r);
        }
        separator = ',';
    }

    private int categorise(char c)
    {
        switch(c)
        {
        case 13: // '\r'
        case 32: // ' '
        case 255:
            return 4;

        case 10: // '\n'
        case 35: // '#'
            return 0;

        case 34: // '"'
            return 2;
        }
        if(c == separator)
        {
            return 3;
        }
        if('!' <= c && c <= '~')
        {
            return 1;
        }
        if('\0' <= c && c <= ' ')
        {
            return 4;
        }
        return !Character.isWhitespace(c) ? 1 : 4;
    }

    public String[] getLine()
    {
        Vector lineArray = new Vector();
        String token = null;
        String returnArray[] = null;
        try
        {
            while(lineArray.size() == 0)
            {
                while((token = get()) != null)
                {
                    lineArray.add(token);
                }
            }
        }
        catch(EOFException e)
        {
            return null;
        }
        catch(IOException e) { }
        returnArray = new String[lineArray.size()];
        for(int ii = 0; ii < lineArray.size(); ii++)
        {
            returnArray[ii] = lineArray.elementAt(ii).toString();
        }

        return returnArray;
    }

    private String get()
        throws EOFException, IOException
    {
        StringBuffer field = new StringBuffer(50);
        readLine();
        int state = 0;
        for(int i = 0; i < line.length(); i++)
        {
            char c = line.charAt(i);
            int category = categorise(c);
            switch(state)
            {
            default:
                break;

            case 0: // '\0'
                switch(category)
                {
                case 2: // '\002'
                    state = 2;
                    break;

                case 3: // '\003'
                    line = line.substring(i + 1);
                    return "";

                case 0: // '\0'
                    line = null;
                    return null;

                case 1: // '\001'
                    field.append(c);
                    state = 1;
                    break;
                }
                break;

            case 1: // '\001'
                switch(category)
                {
                case 2: // '\002'
                    throw new IOException("Malformed CSV stream. Missing quote at start of field on line " + lineCount);

                case 3: // '\003'
                    line = line.substring(i + 1);
                    return field.toString().trim();

                case 0: // '\0'
                    line = line.substring(i);
                    return field.toString().trim();

                case 4: // '\004'
                    field.append(' ');
                    break;

                case 1: // '\001'
                    field.append(c);
                    break;
                }
                break;

            case 2: // '\002'
                switch(category)
                {
                case 2: // '\002'
                    state = 3;
                    break;

                case 0: // '\0'
                    throw new IOException("Malformed CSV stream. Missing quote after field on line " + lineCount);

                case 4: // '\004'
                    field.append(' ');
                    break;

                case 1: // '\001'
                case 3: // '\003'
                    field.append(c);
                    break;
                }
                break;

            case 3: // '\003'
                switch(category)
                {
                case 2: // '\002'
                    field.append(c);
                    state = 2;
                    break;

                case 3: // '\003'
                    line = line.substring(i + 1);
                    return field.toString().trim();

                case 0: // '\0'
                    line = line.substring(i);
                    return field.toString().trim();

                case 4: // '\004'
                    state = 4;
                    break;

                case 1: // '\001'
                    throw new IOException("Malformed CSV stream, missing separator after field on line " + lineCount);
                }
                break;

            case 4: // '\004'
                switch(category)
                {
                case 3: // '\003'
                    line = line.substring(i + 1);
                    return field.toString().trim();

                case 0: // '\0'
                    line = line.substring(i);
                    return field.toString().trim();

                case 1: // '\001'
                case 2: // '\002'
                    throw new IOException("Malformed CSV stream, missing separator after field on line " + lineCount);
                }
                break;
            }
        }

        throw new IOException("Program logic bug. Should not reach here. Processing line " + lineCount);
    }

    private void readLine()
        throws EOFException, IOException
    {
        if(line == null)
        {
            line = r.readLine();
            if(line == null)
            {
                throw new EOFException();
            }
            line += 10;
            lineCount++;
        }
    }

    public void skip(int fields)
        throws EOFException, IOException
    {
        if(fields <= 0)
        {
            return;
        }
        for(int i = 0; i < fields; i++)
        {
            get();
        }

    }

    public void skipToNextLine()
        throws EOFException, IOException
    {
        if(line == null)
        {
            readLine();
        }
        line = null;
    }

    public void close()
        throws IOException
    {
        if(r != null)
        {
            r.close();
            r = null;
        }
    }

    private static void testSingleTokens(String args[])
    {
        try
        {
            CSVReader csv = new CSVReader(new FileReader(args[0]), ',');
            try
            {
                do
                {
                    System.out.println(csv.get());
                } while(true);
            }
            catch(EOFException e)
            {
                csv.close();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static void testLines(String args)  throws Exception
    {
        int lineCounter = 0;
        String loadLine[] = null;
        String DEL = ",";
         CSVReader csv = null;
        try
        {
            csv = new CSVReader(new FileReader(args), ',');
            System.out.println("CSV : "+csv.getLine().length);
            while((loadLine = csv.getLine()) != null)
            {
                lineCounter++;
                StringBuffer logBuffer = new StringBuffer();
                logBuffer.append(loadLine[0]);

                for(int i = 1; i < loadLine.length; i++)
                {
                    logBuffer.append(DEL).append(loadLine[i]);
                }

                String logLine = logBuffer.toString();
                System.out.println("Log Line is : "+logLine);
                logLine.substring(0, logLine.lastIndexOf(DEL));

                csv.skipToNextLine();
            }
            csv.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }finally{
            csv.close();
            System.out.println("CSV Closed");
        }
    }

    public static void main(String args[]) throws Exception
    {
        String csvFile = "C:\\Documents and Settings\\ctikkis\\Desktop\\Temp\\Playbook\\MissingFolders.csv" ;
        //String csvFile = "C:\\Documents and Settings\\ctikkis.CG\\My Documents\\Projects\\Mexico Project\\CMS_SUB_DOC_CSV.csv" ;

        testLines(csvFile);
    }
}
