import sys
import ast
print(ast.__file__)
import plotly
plotly.tools.set_credentials_file(username='shellemen', api_key='EOXnUv5Y6zlPJJOIFJgD')
import plotly.plotly as py
import plotly.figure_factory as ff
print("hello")

year = 1940
arg = sys.argv[1]
#arg = "[[[3, 8, 5], [0, 13, 3], [5, 16, 10], [2, 27, 9], [4, 36, 3], [1, 54, 10]], [[5, 0, 3], [3, 3, 5], [1, 8, 8], [4, 16, 3], [0, 19, 6], [2, 36, 1]], [[4, 0, 9], [0, 9, 1], [2, 10, 5], [3, 15, 5], [1, 20, 5], [5, 46, 1]], [[5, 3, 3], [2, 15, 4], [3, 20, 3], [0, 25, 7], [4, 39, 1], [1, 64, 4]], [[4, 19, 5], [3, 24, 8], [1, 32, 10], [5, 42, 4], [2, 46, 7], [0, 53, 6]], [[5, 6, 9], [2, 19, 8], [4, 27, 4], [0, 32, 3], [3, 35, 9], [1, 44, 10]]]"
arg2 = ast.literal_eval(arg)
df = []
print(df)
for i in range(len(arg2)):
    for j in range(len(arg2[i])):
        startTime = arg2[i][j][1]
        endTime = startTime + arg2[i][j][2]
        resource = arg2[i][j][0]
        startTime = "" + str(startTime + year)
        endTime = "" + str(endTime + year)
        resource = "Job: " + str(resource)
        task = "Machine: " + str(i)
        df.append(dict(Task=task, Start = startTime, Finish = endTime, Resource = resource))
for i in range(len(df)):
    print(df[i])
df2 = [dict(Task="Job-1", Start='2017-01-01', Finish='2017-02-02', Resource='Complete'),
      dict(Task="Job-1", Start='2017-02-15', Finish='2017-03-15', Resource='Incomplete'),
      dict(Task="Job-2", Start='2017-01-17', Finish='2017-02-17', Resource='Not Started'),
      dict(Task="Job-2", Start='2017-01-17', Finish='2017-02-17', Resource='Complete'),
      dict(Task="Job-3", Start='2017-03-10', Finish='2017-03-20', Resource='Not Started'),
      dict(Task="Job-3", Start='2017-04-01', Finish='2017-04-20', Resource='Not Started'),
      dict(Task="Job-3", Start='2017-05-18', Finish='2017-06-18', Resource='Not Started'),
      dict(Task="Job-4", Start='2017-01-14', Finish='2017-03-14', Resource='Complete')]

colors = {'Job: 0': 'rgb(220, 0, 0)',
    'Job: 1': 'rgb(1, 255, 0)',
        'Job: 2': 'rgb(150, 255, 100)',
            'Job: 3': 'rgb(0, 100, 100)',
                'Job: 4': 'rgb(255, 100, 100)',
                    'Job: 5': 'rgb(0, 100, 255)',
                        'Job: 6': 'rgb(50, 50, 255)',
                            'Job: 7': 'rgb(0, 50, 255)',
                                'Job: 8': 'rgb(50, 0, 255)',
                                    'Job: 9': 'rgb(99, 189, 45)',
                                        'Job: 10': 'rgb(0, 100, 255)'}

fig = ff.create_gantt(df, colors=colors, index_col='Resource', show_colorbar=True, group_tasks=True)
py.iplot(fig, filename='gantt-group-tasks-together')
